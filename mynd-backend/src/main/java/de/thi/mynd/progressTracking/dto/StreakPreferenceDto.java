/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.StreakType;
import lombok.Builder;

@Builder
public final class StreakPreferenceDto {

  public String creatorId;
  public StreakType type;
  public boolean isPublic;
}
