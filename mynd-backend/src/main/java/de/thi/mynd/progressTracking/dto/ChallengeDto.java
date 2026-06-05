package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.ChallengeType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public final class ChallengeDto {
    public UUID id;
    public ChallengeType type;
    public LocalDate startDate;
    public LocalDate endDate;
    public int targetCount;
    public int currentCount;
    public boolean completed;
    public boolean rewardClaimed;
}
