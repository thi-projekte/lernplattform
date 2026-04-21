package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.requests.TopicRequest;
import java.util.List;
import java.util.UUID;

public interface TopicService {

  PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize);

  List<ListTopicDto> findTopicsBySearchMax5(String search);

  TopicDto createTopic(TopicRequest request);

  TopicDto updateTopic(UUID topicId, TopicRequest request) throws EntityInstanceNotFoundException;

  TopicDto getTopic(UUID topicId) throws EntityInstanceNotFoundException;

  void deleteTopic(UUID topicId) throws EntityInstanceNotFoundException;
}
