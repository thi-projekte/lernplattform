/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.ChallengeType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

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
