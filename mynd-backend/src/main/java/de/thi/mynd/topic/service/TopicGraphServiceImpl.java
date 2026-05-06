package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicGraphRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicGraphServiceImpl implements TopicGraphService {

  @Inject TopicGraphRepository topicRepository;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(int n) {
    List<Topic> topics = topicRepository.findNMostPopular(n);

    return mappingRegistry.mapList(topics, GraphTopicDto.class);
  }

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
      int n, List<UUID> categoryFilter) {
    List<Topic> topics = topicRepository.findNMostPopularFilterByCategoryIds(n, categoryFilter);

    return mappingRegistry.mapList(topics, GraphTopicDto.class);
  }

  @Override
  public List<GraphTopicDto> getNeighborsOfTopic(UUID topicId) {
    List<Topic> topics = topicRepository.findNeighborsByTopicId(topicId);

    return mappingRegistry.mapList(topics, GraphTopicDto.class);
  }
}
