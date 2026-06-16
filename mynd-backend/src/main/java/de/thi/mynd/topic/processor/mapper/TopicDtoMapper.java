/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.service.ContentElementService;
import de.thi.mynd.topic.service.IndexCardService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public final class TopicDtoMapper extends AbstractMappingProcessor<Topic, TopicDto> {

  @Inject ContentElementService contentElementService;
  @Inject IndexCardService indexCardService;

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
        .indexCards(indexCardService.getIndexCardsForTopic(entity.id))
        .updatedAt(entity.updatedAt)
        .build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TopicDto mapAndEnrich(Topic entity, Object... additionalData) {
    TopicDto withoutProgressData = this.mapAndEnrich(entity);
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
  public Class<TopicDto> getDtoType() {
    return TopicDto.class;
  }
}
