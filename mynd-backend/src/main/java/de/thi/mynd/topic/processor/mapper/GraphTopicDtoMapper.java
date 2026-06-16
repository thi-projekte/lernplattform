/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.service.LearnProgressService;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public final class GraphTopicDtoMapper extends AbstractMappingProcessor<Topic, GraphTopicDto> {

  @Inject LearnProgressService learnProgressService;

  @Override
  public GraphTopicDto mapAndEnrich(Topic entity) {

    List<UUID> associatedTopicIds = new ArrayList<>();
    associatedTopicIds.addAll(
        entity.ownedAssociations.stream().map(a -> a.foreignTopic.id).toList());
    associatedTopicIds.addAll(
        entity.foreignAssociations.stream().map(a -> a.owningTopic.id).toList());

    return GraphTopicDto.builder()
        .id(entity.id)
        .title(entity.title)
        .categories(entity.categories)
        .updatedAt(entity.updatedAt)
        .creatorId(entity.creatorId)
        .creatorFullName(identityService.getFullNameByUsername(entity.creatorId))
        .associatedTopics(associatedTopicIds)
        .build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public GraphTopicDto mapAndEnrich(Topic entity, Object... additionalData) {
    GraphTopicDto withoutProgressData = this.mapAndEnrich(entity);
    Map<UUID, TopicLearnProgressDto> progressData =
        (Map<UUID, TopicLearnProgressDto>) additionalData[0];

    if (progressData.containsKey(entity.id)) {
      withoutProgressData.learnProgress = progressData.get(entity.id);
    }

    return withoutProgressData;
  }

  @Override
  public Object[] obtainAdditionalData(List<Topic> entities) {
    List<UUID> topicIds = entities.stream().map(e -> e.id).toList();
    Map<UUID, TopicLearnProgressDto> mapping =
        learnProgressService.getLearnProgressMappingForTopics(topicIds);

    return new Object[] {mapping};
  }

  @Override
  public Class<Topic> getEntityType() {
    return Topic.class;
  }

  @Override
  public Class<GraphTopicDto> getDtoType() {
    return GraphTopicDto.class;
  }
}
