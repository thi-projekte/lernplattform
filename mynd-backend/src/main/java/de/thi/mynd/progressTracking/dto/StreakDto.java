package de.thi.mynd.progressTracking.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public class StreakDto {
    public UUID id;
    public String creatorId;
    public LocalDateTime startDate;
    public LocalDateTime endDate;
    public boolean active;
}
