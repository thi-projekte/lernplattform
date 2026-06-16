/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class StreakPreferenceDtoMapper
    extends AbstractMappingProcessor<StreakPreference, StreakPreferenceDto> {
  @Override
  public StreakPreferenceDto mapAndEnrich(StreakPreference entity) {
    return StreakPreferenceDto.builder()
        .creatorId(entity.creatorId)
        .isPublic(entity.isPublic)
        .type(entity.type)
        .build();
  }

  @Override
  public Class<StreakPreference> getEntityType() {
    return StreakPreference.class;
  }

  @Override
  public Class<StreakPreferenceDto> getDtoType() {
    return StreakPreferenceDto.class;
  }
}
