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

import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.entity.StreakType;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Exercises {@link StreakRepository} against a real Postgres instance (Quarkus dev services). */
@QuarkusTest
class StreakRepositoryTest {

  @Inject StreakRepository streakRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private Streak newStreak(String creatorId, StreakType type, LocalDateTime endedAt) {
    Streak s = new Streak();
    s.creatorId = creatorId;
    s.type = type;
    s.startedAt = LocalDateTime.now().minusDays(3);
    s.lastContinuedAt = LocalDateTime.now();
    s.endedAt = endedAt;
    s.streakCount = 3;
    return s;
  }

  @Test
  @TestTransaction
  void findNotEndedByCreatorId_returnsOnlyNotEndedRowsForCreator() {
    String creatorId = unique("creator");
    Streak notEnded = newStreak(creatorId, StreakType.DAILY, null);
    Streak ended = newStreak(creatorId, StreakType.WEEKLY, LocalDateTime.now());
    Streak otherCreator = newStreak(unique("other"), StreakType.DAILY, null);
    streakRepository.persistAndFlush(notEnded);
    streakRepository.persistAndFlush(ended);
    streakRepository.persistAndFlush(otherCreator);

    List<Streak> result = streakRepository.findNotEndedByCreatorId(creatorId);

    assertEquals(1, result.size());
    assertEquals(notEnded.id, result.get(0).id);
  }

  @Test
  @TestTransaction
  void findNotEndedByCreatorId_noMatches_returnsEmptyList() {
    List<Streak> result = streakRepository.findNotEndedByCreatorId(unique("creator"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findNotEndedByCreatorIdAndType_existingNotEndedRow_isFound() {
    String creatorId = unique("creator");
    Streak streak = newStreak(creatorId, StreakType.MONTHLY, null);
    streakRepository.persistAndFlush(streak);

    Optional<Streak> result =
        streakRepository.findNotEndedByCreatorIdAndType(creatorId, StreakType.MONTHLY);

    assertTrue(result.isPresent());
    assertEquals(streak.id, result.get().id);
  }

  @Test
  @TestTransaction
  void findNotEndedByCreatorIdAndType_wrongType_returnsEmpty() {
    String creatorId = unique("creator");
    streakRepository.persistAndFlush(newStreak(creatorId, StreakType.MONTHLY, null));

    Optional<Streak> result =
        streakRepository.findNotEndedByCreatorIdAndType(creatorId, StreakType.DAILY);

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findNotEndedByCreatorIdAndType_endedRow_isExcluded() {
    String creatorId = unique("creator");
    streakRepository.persistAndFlush(newStreak(creatorId, StreakType.DAILY, LocalDateTime.now()));

    Optional<Streak> result =
        streakRepository.findNotEndedByCreatorIdAndType(creatorId, StreakType.DAILY);

    assertTrue(result.isEmpty());
  }
}
