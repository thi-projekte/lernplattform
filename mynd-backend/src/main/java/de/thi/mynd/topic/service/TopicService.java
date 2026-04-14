package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;


public interface TopicService {

    PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize);
}
