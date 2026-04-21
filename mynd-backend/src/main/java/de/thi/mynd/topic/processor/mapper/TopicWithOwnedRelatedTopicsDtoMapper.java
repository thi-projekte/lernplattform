package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.TopicWithOwnedRelatedTopicsDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.service.ContentElementService;
import de.thi.mynd.topic.service.TopicService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class TopicWithOwnedRelatedTopicsDtoMapper extends AbstractMappingProcessor<Topic, TopicWithOwnedRelatedTopicsDto> {

  @Inject ContentElementService contentElementService;

  @Inject
  TopicService topicService;

  @Override
  public TopicWithOwnedRelatedTopicsDto mapAndEnrich(Topic entity) {
    return TopicWithOwnedRelatedTopicsDto.builder()
        .id(entity.id)
        .title(entity.title)
        .teaser(entity.teaser)
        .creatorId(entity.creatorId)
        .creatorFullName(identityService.getFullNameByUsername(entity.creatorId))
        .estimatedLearningDuration(entity.estimatedLearningDuration)
        .categories(entity.categories)
        .contentElements(contentElementService.getContentElementsForTopic(entity.id))
        .updatedAt(entity.updatedAt)
            .relatedTopics(topicService.getOwnedRelatedTopicsForTopic(entity.id))
        .build();
  }

  @Override
  public Class<Topic> getEntityType() {
    return Topic.class;
  }

  @Override
  public Class<TopicWithOwnedRelatedTopicsDto> getDtoType() {
    return TopicWithOwnedRelatedTopicsDto.class;
  }
}
