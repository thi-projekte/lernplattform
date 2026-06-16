/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
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
