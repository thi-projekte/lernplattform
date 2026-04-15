package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.entity.Topic;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class ListTopicDtoMapper extends AbstractMappingProcessor<Topic, ListTopicDto> {

  @Override
  public ListTopicDto mapAndEnrich(Topic topic) {
    return ListTopicDto.builder()
        .id(topic.id)
        .title(topic.title)
        .categories(topic.categories)
        .updatedAt(topic.updatedAt)
        .creatorId(topic.creatorId)
        .creatorFullName(identityService.getFullNameByUsername(topic.creatorId))
        .build();
  }

  @Override
  public Class<ListTopicDto> getDtoType() {
    return ListTopicDto.class;
  }

  @Override
  public Class<Topic> getEntityType() {
    return Topic.class;
  }
}
