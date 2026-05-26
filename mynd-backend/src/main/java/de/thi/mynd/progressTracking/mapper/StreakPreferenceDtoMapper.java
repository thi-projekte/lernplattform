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
