/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.entity.Challenge;
import de.thi.mynd.progressTracking.entity.ChallengeType;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link ChallengeRepository} against a real Postgres instance (Quarkus dev services).
 */
@QuarkusTest
class ChallengeRepositoryTest {

  @Inject ChallengeRepository challengeRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private Challenge newChallenge(
      String creatorId, ChallengeType type, LocalDate startDate, LocalDate endDate) {
    Challenge c = new Challenge();
    c.creatorId = creatorId;
    c.type = type;
    c.startDate = startDate;
    c.endDate = endDate;
    c.targetCount = 10;
    return c;
  }

  @Test
  @TestTransaction
  void findCurrentForUser_todayWithinRange_isFound() {
    String creatorId = unique("creator");
    LocalDate today = LocalDate.now();
    Challenge challenge =
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(2), today.plusDays(2));
    challengeRepository.persistAndFlush(challenge);

    Optional<Challenge> result =
        challengeRepository.findCurrentForUser(creatorId, ChallengeType.WEEKLY, today);

    assertTrue(result.isPresent());
    assertEquals(challenge.id, result.get().id);
  }

  @Test
  @TestTransaction
  void findCurrentForUser_todayOutsideRange_returnsEmpty() {
    String creatorId = unique("creator");
    LocalDate today = LocalDate.now();
    challengeRepository.persistAndFlush(
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(10), today.minusDays(5)));

    Optional<Challenge> result =
        challengeRepository.findCurrentForUser(creatorId, ChallengeType.WEEKLY, today);

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findHistoryForUser_onlyPastChallengesOrderedByEndDateDescRespectingLimit() {
    String creatorId = unique("creator");
    LocalDate today = LocalDate.now();
    Challenge oldest =
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(30), today.minusDays(23));
    Challenge middle =
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(20), today.minusDays(13));
    Challenge newest =
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(10), today.minusDays(3));
    Challenge current =
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(1), today.plusDays(5));
    challengeRepository.persistAndFlush(oldest);
    challengeRepository.persistAndFlush(middle);
    challengeRepository.persistAndFlush(newest);
    challengeRepository.persistAndFlush(current);

    List<Challenge> result = challengeRepository.findHistoryForUser(creatorId, today, 2);

    assertEquals(2, result.size());
    assertEquals(newest.id, result.get(0).id);
    assertEquals(middle.id, result.get(1).id);
  }

  @Test
  @TestTransaction
  void findHistoryForUser_noPastChallenges_returnsEmpty() {
    String creatorId = unique("creator");
    LocalDate today = LocalDate.now();
    challengeRepository.persistAndFlush(
        newChallenge(creatorId, ChallengeType.WEEKLY, today.minusDays(1), today.plusDays(5)));

    List<Challenge> result = challengeRepository.findHistoryForUser(creatorId, today, 10);

    assertTrue(result.isEmpty());
  }
}
