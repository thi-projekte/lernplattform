package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LearnProgressService {

  Map<UUID, TopicLearnProgressDto> getLearnProgressMappingForTopics(List<UUID> topicIds);

  TopicLearnProgressDto getLearnProgressForTopic(UUID topicId);

  void startLearningTopicAsCurrentUser(UUID topicId);

  void manuallyCompleteTopicAsCurrentUser(UUID topicId);

  void completeLearningContentElementAsCurrentUser(UUID contentElementId);

  void resetTopicLearningProgress(UUID topicId);

  void resetContentElementLearningProgress(UUID contentElementId);

  List<UUID> getLastNUncompletedTopicsForUser(int n, String creatorId);
}
