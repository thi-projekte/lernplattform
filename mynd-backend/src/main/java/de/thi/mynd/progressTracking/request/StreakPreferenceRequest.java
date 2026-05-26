package de.thi.mynd.progressTracking.request;

import de.thi.mynd.progressTracking.entity.StreakType;
import jakarta.validation.constraints.NotNull;

public final class StreakPreferenceRequest {

  @NotNull public StreakType type;
  @NotNull public Boolean isPublic;
}
