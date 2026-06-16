/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.request.StreakPreferenceRequest;
import java.util.List;

public interface StreakService {

  List<StreakDto> getLatestStreaksForCurrentUser();

  StreakDto getLatestPreferredStreakForUser(String creatorId);

  void continueOrStartStreaksForCurrentUser();

  void endStreaksIfNotActiveAnymore(List<Streak> streaks);

  StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser();

  void updateStreakPreferencesForCurrentUser(StreakPreferenceRequest request);

  boolean isStreakActive(Streak streak);

  boolean isStreakSatisfied(Streak streak);
}
