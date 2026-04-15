package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import java.util.List;

public interface TopicService {

  PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize);

  List<ListTopicDto> findTopicsBySearchMax5(String search);
}
