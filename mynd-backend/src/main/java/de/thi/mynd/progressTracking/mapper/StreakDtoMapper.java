/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.service.StreakService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class StreakDtoMapper extends AbstractMappingProcessor<Streak, StreakDto> {

  @Inject StreakService streakService;

  @Override
  public StreakDto mapAndEnrich(Streak entity) {
    return StreakDto.builder()
        .id(entity.id)
        .creatorId(entity.creatorId)
        .startedAt(entity.startedAt)
        .endedAt(entity.endedAt)
        .type(entity.type)
        .lastContinuedAt(entity.lastContinuedAt)
        .streakCount(entity.streakCount)
        .isActive(streakService.isStreakActive(entity))
        .isSatisfied(streakService.isStreakSatisfied(entity))
        .build();
  }

  @Override
  public Class<Streak> getEntityType() {
    return Streak.class;
  }

  @Override
  public Class<StreakDto> getDtoType() {
    return StreakDto.class;
  }
}
