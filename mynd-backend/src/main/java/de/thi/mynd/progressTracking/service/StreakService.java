package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.Streak;

import java.util.List;

public interface StreakService {

    List<StreakDto> getLatestStreaksForCurrentUser();

    void continueOrStartStreaksForCurrentUser();

    StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser();

    void updateStreakPreferencesForCurrentUser();

    boolean isStreakActive(Streak streak);

    boolean isStreakSatisfied(Streak streak);
}
