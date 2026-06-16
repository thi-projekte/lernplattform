/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.request.TopicRequest;
import java.util.List;
import java.util.UUID;

public interface TopicService {

  PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize);

  List<ListTopicDto> findTopicsBySearchMax5(String search);

  List<ListTopicDto> getOwnedRelatedTopicsForTopic(UUID topicId);

  TopicDto createTopic(TopicRequest request);

  TopicDto updateTopic(UUID topicId, TopicRequest request) throws EntityInstanceNotFoundException;

  TopicDto getTopic(UUID topicId, boolean withOwnedRelatedTopics)
      throws EntityInstanceNotFoundException;

  void deleteTopic(UUID topicId) throws EntityInstanceNotFoundException;
}
