package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.StreakType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public final class StreakDto {
  public UUID id;
  public String creatorId;
  public LocalDateTime startedAt;
  public LocalDateTime endedAt;
  public StreakType type;
  public LocalDateTime lastContinuedAt;
  public long streakCount;
  public boolean isActive;
  public boolean isSatisfied;
}
