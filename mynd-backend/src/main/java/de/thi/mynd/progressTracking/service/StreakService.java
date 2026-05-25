package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;

import java.util.List;

public interface StreakService {

    List<StreakDto> getActiveStreaksForCurrentUser();

    void continueOrStartStreaksForCurrentUser();

    StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser();

    void updateStreakPreferencesForCurrentUser();
}
