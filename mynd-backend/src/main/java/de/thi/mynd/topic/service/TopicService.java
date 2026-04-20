package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.requests.CreateTopicRequest;
import java.util.List;

public interface TopicService {

  PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize);

  List<ListTopicDto> findTopicsBySearchMax5(String search);

  TopicDto createTopic(CreateTopicRequest request);
}
