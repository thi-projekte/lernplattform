package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.request.StreakPreferenceRequest;
import java.util.List;

public interface StreakService {

  List<StreakDto> getLatestStreaksForCurrentUser();

  void continueOrStartStreaksForCurrentUser();

  void endStreaksIfNotActiveAnymore(List<Streak> streaks);

  StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser();

  void updateStreakPreferencesForCurrentUser(StreakPreferenceRequest request);

  boolean isStreakActive(Streak streak);

  boolean isStreakSatisfied(Streak streak);
}
