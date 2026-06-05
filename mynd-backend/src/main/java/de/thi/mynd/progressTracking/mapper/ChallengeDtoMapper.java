package de.thi.mynd.progressTracking.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.entity.Challenge;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class ChallengeDtoMapper extends AbstractMappingProcessor<Challenge, ChallengeDto> {
    @Override
    public ChallengeDto mapAndEnrich(Challenge entity) {
        return null;
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
