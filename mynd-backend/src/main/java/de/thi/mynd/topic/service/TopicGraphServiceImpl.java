package de.thi.mynd.topic.service;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicGraphRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
public final class TopicGraphServiceImpl implements TopicGraphService {

  @Inject TopicGraphRepository topicGraphRepository;

  @Inject TopicRepository topicRepository;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(int n) {
    List<Topic> topics = topicGraphRepository.findNMostPopular(n);

    return getGraphTopicDtosWithNeighbors(topics);
  }

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
      int n, List<UUID> categoryFilter) {
    List<Topic> topics =
        topicGraphRepository.findNMostPopularFilterByCategoryIds(n, categoryFilter);

    return getGraphTopicDtosWithNeighbors(topics);
  }

  @Override
  public List<GraphTopicDto> getNeighborsOfTopic(UUID topicId)
      throws EntityInstanceNotFoundException {
    Optional<Topic> topicOptional = topicRepository.findByIdOptional(topicId);

    if (topicOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("Topic does not exist");
    }

    Topic topic = topicOptional.get();

    List<Topic> topics =
        Stream.concat(
                topic.ownedAssociations.stream().map(a -> a.foreignTopic),
                topic.foreignAssociations.stream().map(a -> a.owningTopic))
            .toList();

    return mappingRegistry.mapList(topics, GraphTopicDto.class);
  }

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(int n, String creatorId) {
    List<Topic> topics = topicGraphRepository.findNMostPopular(n, creatorId);

    return getGraphTopicDtosWithNeighbors(topics, creatorId);
  }

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(int n, List<UUID> categoryFilter, String creatorId) {
    List<Topic> topics =
            topicGraphRepository.findNMostPopularFilterByCategoryIds(n, categoryFilter, creatorId);

    return getGraphTopicDtosWithNeighbors(topics, creatorId);
  }

  @Override
  public List<GraphTopicDto> getOwnedNeighborsOfTopic(UUID topicId) throws EntityInstanceNotFoundException {
    Optional<Topic> topicOptional = topicRepository.findByIdOptional(topicId);

    if (topicOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("Topic does not exist");
    }

    Topic topic = topicOptional.get();

    List<Topic> topics = topic.ownedAssociations.stream().map(a -> a.foreignTopic).toList();

    return mappingRegistry.mapList(topics, GraphTopicDto.class);
  }

  @Override
  public List<GraphTopicDto> searchTopicNodes(String search, int limit) {
    List<Topic> topics = topicRepository.findBySearch(search, limit);

    return mappingRegistry.mapList(topics, GraphTopicDto.class);
  }

  private List<GraphTopicDto> getGraphTopicDtosWithNeighbors(List<Topic> topics) {
    List<GraphTopicDto> mapped = mappingRegistry.mapList(topics, GraphTopicDto.class);
    Set<GraphTopicDto> uniqueMapped = new HashSet<>(mapped);
    for (Topic topic : topics) {
      uniqueMapped.addAll(getNeighborsOfTopic(topic.id));
    }

    return uniqueMapped.stream().toList();
  }

  private List<GraphTopicDto> getGraphTopicDtosWithNeighbors(List<Topic> topics, String creatorId) {
    List<GraphTopicDto> mapped = mappingRegistry.mapList(topics, GraphTopicDto.class);
    Set<GraphTopicDto> uniqueMapped = new HashSet<>(mapped);
    for (Topic topic : topics) {
      uniqueMapped.addAll(getOwnedNeighborsOfTopic(topic.id));
    }

    return uniqueMapped.stream().toList();
  }
}
