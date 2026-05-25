package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.StreakType;
import lombok.Builder;

@Builder
public class StreakPreferenceDto {

    public String creatorId;
    public StreakType type;
    public int allowedCheatUnits;
    public boolean isPublic;
}
