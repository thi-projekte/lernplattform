package de.thi.mynd.progressTracking.service;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.repository.UserProfileRepository;
import de.thi.mynd.auth.service.UserProfileService;
import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.entity.Challenge;
import de.thi.mynd.progressTracking.entity.ChallengeType;
import de.thi.mynd.progressTracking.repository.ChallengeRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class ChallengeServiceImpl implements ChallengeService {


  @ConfigProperty(name = "mynd.challenges.weeklyTarget")
  int weeklyTarget;

  @ConfigProperty(name = "mynd.challenges.rewardInvitations")
  int rewardInvitations;


  @Inject ChallengeRepository challengeRepository;
  @Inject SecurityIdentity identity;
  @Inject
  UserProfileService userProfileService;
  @Inject
  MappingRegistry mappingRegistry;

  @Override
  @Transactional
  public ChallengeDto getCurrentChallenge() {
    String creatorId = identity.getPrincipal().getName();
    LocalDate today = LocalDate.now();
    Challenge challenge =
        challengeRepository
            .findCurrentForUser(creatorId, ChallengeType.WEEKLY, today)
            .orElseGet(() -> createWeeklyChallenge(creatorId, today));
    return mappingRegistry.map(challenge, ChallengeDto.class);
  }

  @Override
  @Transactional
  public void trackContentElementCompletion() {
    String creatorId = identity.getPrincipal().getName();
    LocalDate today = LocalDate.now();
    challengeRepository
        .findCurrentForUser(creatorId, ChallengeType.WEEKLY, today)
        .ifPresent(
            challenge -> {
              if (!challenge.completed) {
                challenge.currentCount++;
                if (challenge.currentCount >= challenge.targetCount) {
                  challenge.completed = true;
                }
                challengeRepository.persistAndFlush(challenge);
              }
            });
  }

  @Override
  @Transactional
  public ChallengeDto claimReward(UUID challengeId) {
    Challenge challenge = getChallenge(challengeId);

    challenge.rewardClaimed = true;
    challengeRepository.persistAndFlush(challenge);

    UserProfile profile = userProfileService.getPersonalUserProfile()
            .orElseGet(() -> userProfileService.createPersonalUserProfile());
    profile.invitationsLeft += rewardInvitations;
    userProfileService.updateUserProfile(profile);

    return mappingRegistry.map(challenge, ChallengeDto.class);
  }

  @Override
  public List<ChallengeDto> getChallengeHistory() {
    String creatorId = identity.getPrincipal().getName();
    List<Challenge> challenges =  challengeRepository.findHistoryForUser(creatorId, LocalDate.now());
    return mappingRegistry.mapList(challenges, ChallengeDto.class);
  }

  private @NonNull Challenge getChallenge(UUID challengeId) {
    String creatorId = identity.getPrincipal().getName();
    Challenge challenge =
            challengeRepository.findByIdOptional(challengeId)
                    .orElseThrow(() -> new EntityNotFoundException("Challenge does not exist"));

    if (!challenge.creatorId.equals(creatorId)) throw new BadRequestException("This is not your challenge");
    if (!challenge.completed) throw new BadRequestException("Challenge not completed yet");
    if (challenge.rewardClaimed) throw new BadRequestException("Reward already claimed");
    return challenge;
  }

  private Challenge createWeeklyChallenge(String creatorId, LocalDate today) {
    LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    Challenge challenge = new Challenge();
    challenge.creatorId = creatorId;
    challenge.type = ChallengeType.WEEKLY;
    challenge.startDate = start;
    challenge.endDate = start.plusDays(6);
    challenge.targetCount = weeklyTarget;
    challengeRepository.persistAndFlush(challenge);
    return challenge;
  }
}
