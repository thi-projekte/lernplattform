package de.thi.mynd.topic.service;

import de.thi.mynd.topic.entity.Topic;

import java.util.List;

public interface TopicService {

    List<Topic> findPersonalTopicsPaginated(int page, int pageSize);
}
