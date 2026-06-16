/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.service.UserProfileService;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.entity.Challenge;
import de.thi.mynd.progressTracking.entity.ChallengeType;
import de.thi.mynd.progressTracking.repository.ChallengeRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ChallengeServiceImplTest {

  @Inject ChallengeServiceImpl challengeService;

  @InjectMock ChallengeRepository challengeRepository;

  @InjectMock SecurityIdentity identity;

  @InjectMock UserProfileService userProfileService;

  @InjectMock MappingRegistry mappingRegistry;

  private static final String USER_ID = "user-123";
  private Principal mockPrincipal;

  @BeforeEach
  void setUp() {
    mockPrincipal = mock(Principal.class);
    when(mockPrincipal.getName()).thenReturn(USER_ID);
    when(identity.getPrincipal()).thenReturn(mockPrincipal);

    // Injecting config values manually if QuarkusTest properties don't match production defaults
    challengeService.weeklyTarget = 5;
    challengeService.rewardInvitations = 3;
  }

  @Nested
  class GetCurrentChallengeTests {

    @Test
    void shouldReturnExistingChallenge() {
      Challenge existingChallenge = new Challenge();
      ChallengeDto expectedDto = ChallengeDto.builder().build();

      when(challengeRepository.findCurrentForUser(
              eq(USER_ID), eq(ChallengeType.WEEKLY), any(LocalDate.class)))
          .thenReturn(Optional.of(existingChallenge));
      when(mappingRegistry.map(existingChallenge, ChallengeDto.class)).thenReturn(expectedDto);

      ChallengeDto result = challengeService.getCurrentChallenge();

      assertNotNull(result);
      assertEquals(expectedDto, result);
      verify(challengeRepository, never()).persistAndFlush(any(Challenge.class));
    }

    @Test
    void shouldCreateNewChallengeIfNoneExists() {
      ChallengeDto expectedDto = ChallengeDto.builder().build();

      when(challengeRepository.findCurrentForUser(
              eq(USER_ID), eq(ChallengeType.WEEKLY), any(LocalDate.class)))
          .thenReturn(Optional.empty());
      when(mappingRegistry.map(any(Challenge.class), eq(ChallengeDto.class)))
          .thenReturn(expectedDto);

      ChallengeDto result = challengeService.getCurrentChallenge();

      assertNotNull(result);
      assertEquals(expectedDto, result);
      verify(challengeRepository)
          .persistAndFlush(
              argThat(
                  challenge ->
                      challenge.creatorId.equals(USER_ID)
                          && challenge.type == ChallengeType.WEEKLY
                          && challenge.targetCount == 7
                          && challenge.startDate.equals(
                              LocalDate.now()
                                  .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))));
    }
  }

  @Nested
  class TrackContentElementCompletionTests {

    @Test
    void shouldIncrementCountWhenNotCompleted() {
      Challenge challenge = new Challenge();
      challenge.completed = false;
      challenge.currentCount = 2;
      challenge.targetCount = 5;

      when(challengeRepository.findCurrentForUser(
              eq(USER_ID), eq(ChallengeType.WEEKLY), any(LocalDate.class)))
          .thenReturn(Optional.of(challenge));

      challengeService.trackContentElementCompletion();

      assertEquals(3, challenge.currentCount);
      assertFalse(challenge.completed);
      verify(challengeRepository).persistAndFlush(challenge);
    }

    @Test
    void shouldMarkAsCompletedWhenTargetReached() {
      Challenge challenge = new Challenge();
      challenge.completed = false;
      challenge.currentCount = 4;
      challenge.targetCount = 5;

      when(challengeRepository.findCurrentForUser(
              eq(USER_ID), eq(ChallengeType.WEEKLY), any(LocalDate.class)))
          .thenReturn(Optional.of(challenge));

      challengeService.trackContentElementCompletion();

      assertEquals(5, challenge.currentCount);
      assertTrue(challenge.completed);
      verify(challengeRepository).persistAndFlush(challenge);
    }

    @Test
    void shouldDoNothingIfAlreadyCompleted() {
      Challenge challenge = new Challenge();
      challenge.completed = true;
      challenge.currentCount = 5;
      challenge.targetCount = 5;

      when(challengeRepository.findCurrentForUser(
              eq(USER_ID), eq(ChallengeType.WEEKLY), any(LocalDate.class)))
          .thenReturn(Optional.of(challenge));

      challengeService.trackContentElementCompletion();

      assertEquals(5, challenge.currentCount);
      verify(challengeRepository, never()).persistAndFlush(any(Challenge.class));
    }
  }

  @Nested
  class ClaimRewardTests {

    @Test
    void shouldClaimRewardSuccessfullyWithExistingProfile() {
      UUID challengeId = UUID.randomUUID();
      Challenge challenge = new Challenge();
      challenge.creatorId = USER_ID;
      challenge.completed = true;
      challenge.rewardClaimed = false;

      UserProfile userProfile = new UserProfile();
      userProfile.invitationsLeft = 2;

      ChallengeDto expectedDto = ChallengeDto.builder().build();

      when(challengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(challenge));
      when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.of(userProfile));
      when(mappingRegistry.map(challenge, ChallengeDto.class)).thenReturn(expectedDto);

      ChallengeDto result = challengeService.claimReward(challengeId);

      assertTrue(challenge.rewardClaimed);
      assertEquals(3, userProfile.invitationsLeft); // 1 original + 3 rewardInvitations
      verify(challengeRepository).persistAndFlush(challenge);
      verify(userProfileService).updateUserProfile(userProfile);
      assertEquals(expectedDto, result);
    }

    @Test
    void shouldClaimRewardSuccessfullyAndCreateProfileIfMissing() {
      UUID challengeId = UUID.randomUUID();
      Challenge challenge = new Challenge();
      challenge.creatorId = USER_ID;
      challenge.completed = true;
      challenge.rewardClaimed = false;

      UserProfile newProfile = new UserProfile();
      newProfile.invitationsLeft = 0;

      when(challengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(challenge));
      when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.empty());
      when(userProfileService.createPersonalUserProfile()).thenReturn(newProfile);

      challengeService.claimReward(challengeId);

      assertEquals(1, newProfile.invitationsLeft);
      verify(userProfileService).createPersonalUserProfile();
      verify(userProfileService).updateUserProfile(newProfile);
    }

    @Test
    void shouldThrowExceptionIfChallengeNotFound() {
      UUID challengeId = UUID.randomUUID();
      when(challengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.empty());

      assertThrows(EntityNotFoundException.class, () -> challengeService.claimReward(challengeId));
    }

    @Test
    void shouldThrowExceptionIfNotOwner() {
      UUID challengeId = UUID.randomUUID();
      Challenge challenge = new Challenge();
      challenge.creatorId = "wrong-user";

      when(challengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(challenge));

      BadRequestException exception =
          assertThrows(BadRequestException.class, () -> challengeService.claimReward(challengeId));
      assertEquals("This is not your challenge", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNotCompleted() {
      UUID challengeId = UUID.randomUUID();
      Challenge challenge = new Challenge();
      challenge.creatorId = USER_ID;
      challenge.completed = false;

      when(challengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(challenge));

      BadRequestException exception =
          assertThrows(BadRequestException.class, () -> challengeService.claimReward(challengeId));
      assertEquals("Challenge not completed yet", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfRewardAlreadyClaimed() {
      UUID challengeId = UUID.randomUUID();
      Challenge challenge = new Challenge();
      challenge.creatorId = USER_ID;
      challenge.completed = true;
      challenge.rewardClaimed = true;

      when(challengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(challenge));

      BadRequestException exception =
          assertThrows(BadRequestException.class, () -> challengeService.claimReward(challengeId));
      assertEquals("Reward already claimed", exception.getMessage());
    }
  }

  @Nested
  class GetChallengeHistoryTests {

    @Test
    void shouldReturnHistoryList() {
      List<Challenge> mockChallenges = List.of(new Challenge(), new Challenge());
      List<ChallengeDto> expectedDtos =
          List.of(ChallengeDto.builder().build(), ChallengeDto.builder().build());

      when(challengeRepository.findHistoryForUser(eq(USER_ID), any(LocalDate.class)))
          .thenReturn(mockChallenges);
      when(mappingRegistry.mapList(mockChallenges, ChallengeDto.class)).thenReturn(expectedDtos);

      List<ChallengeDto> result = challengeService.getChallengeHistory();

      assertEquals(2, result.size());
      assertEquals(expectedDtos, result);
    }
  }
}
