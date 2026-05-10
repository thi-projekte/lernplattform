package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.common.service.IdentityService;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class GraphTopicDtoMapper extends AbstractMappingProcessor<Topic, GraphTopicDto> {

  @Inject IdentityService identityService;

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
  public Class<Topic> getEntityType() {
    return Topic.class;
  }

  @Override
  public Class<GraphTopicDto> getDtoType() {
    return GraphTopicDto.class;
  }
}
