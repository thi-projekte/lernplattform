/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.topic.dto.TopicWithOwnedRelatedTopicsDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.service.ContentElementService;
import de.thi.mynd.topic.service.IndexCardService;
import de.thi.mynd.topic.service.TopicService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public final class TopicWithOwnedRelatedTopicsDtoMapper
    extends AbstractMappingProcessor<Topic, TopicWithOwnedRelatedTopicsDto> {

  @Inject ContentElementService contentElementService;

  @Inject TopicService topicService;
  @Inject IndexCardService indexCardService;

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
        .indexCards(indexCardService.getIndexCardsForTopic(entity.id))
        .build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TopicWithOwnedRelatedTopicsDto mapAndEnrich(Topic entity, Object... additionalData) {
    TopicWithOwnedRelatedTopicsDto withoutProgressData = this.mapAndEnrich(entity);
    Map<UUID, TopicLearnProgressDto> progressData =
        (Map<UUID, TopicLearnProgressDto>) additionalData[0];

    if (progressData.containsKey(entity.id)) {
      withoutProgressData.learnProgress = progressData.get(entity.id);
    }

    return withoutProgressData;
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
