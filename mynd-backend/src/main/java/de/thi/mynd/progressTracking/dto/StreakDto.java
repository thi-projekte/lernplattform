package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.StreakType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public class StreakDto {
    public UUID id;
    public String creatorId;
    public LocalDateTime startedAt;
    public LocalDateTime endedAt;
    public StreakType type;
    public LocalDateTime lastContinuedAt;
    public boolean active;
}
