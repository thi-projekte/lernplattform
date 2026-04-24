package de.thi.mynd.topic.dto;

import java.util.List;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class TopicWithOwnedRelatedTopicsDto extends TopicDto {
  public List<ListTopicDto> relatedTopics;
}
