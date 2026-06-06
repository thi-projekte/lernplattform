package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import java.util.List;
import java.util.UUID;

public interface ChallengeService {
  ChallengeDto getCurrentChallenge();

  void trackContentElementCompletion();

  ChallengeDto claimReward(UUID challengeId);

  // TODO: For the future maybe paginate this
  List<ChallengeDto> getChallengeHistory();
}
