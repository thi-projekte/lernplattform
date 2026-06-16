/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

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
