package de.thi.mynd.topic.dto;

import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public final class TopicWithOwnedRelatedTopicsDto extends TopicDto {
    public List<ListTopicDto> relatedTopics;
}
