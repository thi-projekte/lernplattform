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
import static org.mockito.Mockito.*;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.entity.StreakType;
import de.thi.mynd.progressTracking.service.StreakService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StreakDtoMapperTest {

  @Inject StreakDtoMapper streakDtoMapper;

  @InjectMock StreakService streakService;

  private Streak streak() {
    Streak streak = new Streak();
    streak.id = UUID.randomUUID();
    streak.creatorId = "alice";
    streak.startedAt = LocalDateTime.now().minusDays(5);
    streak.endedAt = null;
    streak.type = StreakType.DAILY;
    streak.lastContinuedAt = LocalDateTime.now();
    streak.streakCount = 3;
    return streak;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    Streak streak = streak();
    when(streakService.isStreakActive(streak)).thenReturn(true);
    when(streakService.isStreakSatisfied(streak)).thenReturn(true);

    StreakDto dto = streakDtoMapper.mapAndEnrich(streak);

    assertEquals(streak.id, dto.id);
    assertEquals(streak.creatorId, dto.creatorId);
    assertEquals(streak.startedAt, dto.startedAt);
    assertEquals(streak.endedAt, dto.endedAt);
    assertEquals(streak.type, dto.type);
    assertEquals(streak.lastContinuedAt, dto.lastContinuedAt);
    assertEquals(streak.streakCount, dto.streakCount);
  }

  @Test
  void mapAndEnrich_activeAndSatisfied_setsBothTrue() {
    Streak streak = streak();
    when(streakService.isStreakActive(streak)).thenReturn(true);
    when(streakService.isStreakSatisfied(streak)).thenReturn(true);

    StreakDto dto = streakDtoMapper.mapAndEnrich(streak);

    assertTrue(dto.isActive);
    assertTrue(dto.isSatisfied);
  }

  @Test
  void mapAndEnrich_activeButNotSatisfied_setsActiveTrueSatisfiedFalse() {
    Streak streak = streak();
    when(streakService.isStreakActive(streak)).thenReturn(true);
    when(streakService.isStreakSatisfied(streak)).thenReturn(false);

    StreakDto dto = streakDtoMapper.mapAndEnrich(streak);

    assertTrue(dto.isActive);
    assertFalse(dto.isSatisfied);
  }

  @Test
  void mapAndEnrich_notActiveAndNotSatisfied_setsBothFalse() {
    Streak streak = streak();
    when(streakService.isStreakActive(streak)).thenReturn(false);
    when(streakService.isStreakSatisfied(streak)).thenReturn(false);

    StreakDto dto = streakDtoMapper.mapAndEnrich(streak);

    assertFalse(dto.isActive);
    assertFalse(dto.isSatisfied);
  }

  @Test
  void mapAndEnrich_notActiveButSatisfied_setsActiveFalseSatisfiedTrue() {
    Streak streak = streak();
    when(streakService.isStreakActive(streak)).thenReturn(false);
    when(streakService.isStreakSatisfied(streak)).thenReturn(true);

    StreakDto dto = streakDtoMapper.mapAndEnrich(streak);

    assertFalse(dto.isActive);
    assertTrue(dto.isSatisfied);
  }

  @Test
  void getEntityType_returnsStreak() {
    assertEquals(Streak.class, streakDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsStreakDto() {
    assertEquals(StreakDto.class, streakDtoMapper.getDtoType());
  }
}
