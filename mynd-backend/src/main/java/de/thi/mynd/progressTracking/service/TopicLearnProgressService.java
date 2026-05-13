package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TopicLearnProgressService {

  Map<UUID, TopicLearnProgressDto> getLearnProgressMappingForTopics(List<UUID> topicIds);

  TopicLearnProgressDto getLearnProgressForTopic(UUID topicId);

  void startLearningTopicAsCurrentUser(UUID topicId);

  void manuallyCompleteTopicAsCurrentUser(UUID topicId);

  void completeLearningContentElementAsCurrentUser(UUID contentElementId);
}
