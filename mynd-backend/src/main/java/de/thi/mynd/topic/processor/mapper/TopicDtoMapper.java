package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.service.ContentElementService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class TopicDtoMapper extends AbstractMappingProcessor<Topic, TopicDto> {

  @Inject
  ContentElementService contentElementService;

  @Override
  public TopicDto mapAndEnrich(Topic entity) {
    return TopicDto.builder()
        .id(entity.id)
        .title(entity.title)
        .teaser(entity.teaser)
        .creatorId(entity.creatorId)
        .creatorFullName(identityService.getFullNameByUsername(entity.creatorId))
        .estimatedLearningDuration(entity.estimatedLearningDuration)
        .categories(entity.categories)
        .contentElements(contentElementService.getContentElementsForTopic(entity.id))
        .updatedAt(entity.updatedAt)
        .build();
  }

  @Override
  public Class<Topic> getEntityType() {
    return Topic.class;
  }

  @Override
  public Class<TopicDto> getDtoType() {
    return TopicDto.class;
  }
}
