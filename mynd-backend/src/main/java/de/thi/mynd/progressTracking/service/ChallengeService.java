package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import java.util.List;

public interface ChallengeService {
    ChallengeDto getCurrentChallenge();
    void trackContentElementCompletion();
    ChallengeDto claimReward();
    List<ChallengeDto> getChallengeHistory();
}