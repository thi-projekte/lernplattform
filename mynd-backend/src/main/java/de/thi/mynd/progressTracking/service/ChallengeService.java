/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import java.util.List;
import java.util.UUID;

public interface ChallengeService {
  ChallengeDto getCurrentChallenge();

  void trackContentElementCompletion();

  ChallengeDto claimReward(UUID challengeId);

  List<ChallengeDto> getChallengeHistory();
}
