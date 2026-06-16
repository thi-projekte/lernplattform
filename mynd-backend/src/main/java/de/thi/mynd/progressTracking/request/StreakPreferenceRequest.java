/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.request;

import de.thi.mynd.progressTracking.entity.StreakType;
import jakarta.validation.constraints.NotNull;

public final class StreakPreferenceRequest {

  @NotNull public StreakType type;
  @NotNull public Boolean isPublic;
}
