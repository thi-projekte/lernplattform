package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import java.util.List;

public interface ChallengeService {
  ChallengeDto getCurrentChallenge();

  void trackContentElementCompletion();

  ChallengeDto claimReward();

  // TODO: For the future maybe paginate this
  List<ChallengeDto> getChallengeHistory();
}
