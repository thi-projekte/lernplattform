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
import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.entity.Challenge;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class ChallengeDtoMapper extends AbstractMappingProcessor<Challenge, ChallengeDto> {
  @Override
  public ChallengeDto mapAndEnrich(Challenge entity) {
    return ChallengeDto.builder()
        .id(entity.id)
        .type(entity.type)
        .startDate(entity.startDate)
        .endDate(entity.endDate)
        .targetCount(entity.targetCount)
        .currentCount(entity.currentCount)
        .completed(entity.completed)
        .rewardClaimed(entity.rewardClaimed)
        .build();
  }

  @Override
  public Class<Challenge> getEntityType() {
    return Challenge.class;
  }

  @Override
  public Class<ChallengeDto> getDtoType() {
    return ChallengeDto.class;
  }
}
