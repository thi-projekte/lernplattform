/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.entity.Challenge;
import de.thi.mynd.progressTracking.entity.ChallengeType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ChallengeDtoMapperTest {

  @Inject ChallengeDtoMapper challengeDtoMapper;

  private Challenge challenge() {
    Challenge challenge = new Challenge();
    challenge.id = UUID.randomUUID();
    challenge.type = ChallengeType.WEEKLY;
    challenge.startDate = LocalDate.now();
    challenge.endDate = LocalDate.now().plusDays(7);
    challenge.targetCount = 10;
    challenge.currentCount = 4;
    challenge.completed = false;
    challenge.rewardClaimed = false;
    return challenge;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    Challenge challenge = challenge();

    ChallengeDto dto = challengeDtoMapper.mapAndEnrich(challenge);

    assertEquals(challenge.id, dto.id);
    assertEquals(challenge.type, dto.type);
    assertEquals(challenge.startDate, dto.startDate);
    assertEquals(challenge.endDate, dto.endDate);
    assertEquals(challenge.targetCount, dto.targetCount);
    assertEquals(challenge.currentCount, dto.currentCount);
    assertEquals(challenge.completed, dto.completed);
    assertEquals(challenge.rewardClaimed, dto.rewardClaimed);
  }

  @Test
  void mapAndEnrich_completedAndRewardClaimed_copiedAsTrue() {
    Challenge challenge = challenge();
    challenge.completed = true;
    challenge.rewardClaimed = true;

    ChallengeDto dto = challengeDtoMapper.mapAndEnrich(challenge);

    assertTrue(dto.completed);
    assertTrue(dto.rewardClaimed);
  }

  @Test
  void getEntityType_returnsChallenge() {
    assertEquals(Challenge.class, challengeDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsChallengeDto() {
    assertEquals(ChallengeDto.class, challengeDtoMapper.getDtoType());
  }
}
