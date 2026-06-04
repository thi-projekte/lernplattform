package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.ChallengeType;
import java.time.LocalDate;
import java.util.UUID;

public record ChallengeDto(
        UUID id,
        ChallengeType type,
        LocalDate startDate,
        LocalDate endDate,
        int targetCount,
        int currentCount,
        boolean completed,
        boolean rewardClaimed
) {}