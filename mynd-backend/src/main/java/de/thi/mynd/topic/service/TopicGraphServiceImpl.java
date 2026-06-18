/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.service;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.service.LearnProgressService;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicGraphRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
public final class TopicGraphServiceImpl implements TopicGraphService {

  @Inject TopicGraphRepository topicGraphRepository;

  @Inject LearnProgressService learnProgressService;

  @Inject TopicRepository topicRepository;

  @Inject MappingRegistry mappingRegistry;

  @Inject SecurityIdentity identity;

  @Override
  public List<GraphTopicDto> getLearnGraph() {
    String creatorId = identity.getPrincipal().getName();
    Log.tracef("Fetching learn graph for user %s", creatorId);

    List<UUID> lastLearnedTopicIds =
        learnProgressService.getLastNLearnedTopicsForUser(10, creatorId);
    if (lastLearnedTopicIds.isEmpty()) {
      List<Topic> topics = topicGraphRepository.findNMostPopular(10);

      return getGraphTopicDtosWithNeighbors(topics);
    }
    List<Topic> initialTopics = topicRepository.findByIdsTypeSafe(lastLearnedTopicIds);
    Set<GraphTopicDto> dtos = new HashSet<>(getGraphTopicDtosWithNeighbors(initialTopics));

    List<Topic> topicGraph =
        topicGraphRepository.findForAssociatedTopicsNotStartedAllCompleted(
            lastLearnedTopicIds, creatorId);
    List<GraphTopicDto> additionalDtos =
        mappingRegistry.mapListWithAdditionalData(topicGraph, GraphTopicDto.class);

    dtos.addAll(additionalDtos);
    return dtos.stream().toList();
  }

  @Override
  public List<GraphTopicDto> getNeighborsOfTopic(UUID topicId)
      throws EntityInstanceNotFoundException {
    Log.tracef("Loading neighbors for topic", topicId);
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

    return mappingRegistry.mapListWithAdditionalData(topics, GraphTopicDto.class);
  }

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
      int n, String creatorId) {
    Log.tracef(
        "Loading %d most popular topics in graph and their direct neighbors for user %s",
        n, creatorId);
    List<Topic> topics = topicGraphRepository.findNMostPopular(n, creatorId);

    return getGraphTopicDtosWithOwnedNeighbors(topics);
  }

  @Override
  public List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
      int n, List<UUID> categoryFilter, String creatorId) {
    Log.tracef(
        "Loading %d most popular topics in graph and their direct neighbors for user %s",
        n, creatorId);
    List<Topic> topics =
        topicGraphRepository.findNMostPopularFilterByCategoryIds(n, categoryFilter, creatorId);

    return getGraphTopicDtosWithOwnedNeighbors(topics);
  }

  @Override
  public List<GraphTopicDto> getOwnedNeighborsOfTopic(UUID topicId)
      throws EntityInstanceNotFoundException {
    Log.tracef("Loading topic dtos for neighbors of topic %s", topicId);
    Optional<Topic> topicOptional = topicRepository.findByIdOptional(topicId);

    if (topicOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("Topic does not exist");
    }

    Topic topic = topicOptional.get();

    List<Topic> topics = topic.ownedAssociations.stream().map(a -> a.foreignTopic).toList();

    return mappingRegistry.mapListWithAdditionalData(topics, GraphTopicDto.class);
  }

  @Override
  public List<GraphTopicDto> searchTopicNodes(String search, int limit) {
    Log.tracef("Searching topic nodes for %s with limit %s", search, limit);
    List<Topic> topics = topicRepository.findBySearch(search, limit);

    return mappingRegistry.mapListWithAdditionalData(topics, GraphTopicDto.class);
  }

  private List<GraphTopicDto> getGraphTopicDtosWithNeighbors(List<Topic> topics) {
    List<GraphTopicDto> mapped =
        mappingRegistry.mapListWithAdditionalData(topics, GraphTopicDto.class);
    Set<GraphTopicDto> uniqueMapped = new HashSet<>(mapped);
    for (Topic topic : topics) {
      uniqueMapped.addAll(getNeighborsOfTopic(topic.id));
    }

    return uniqueMapped.stream().toList();
  }

  private List<GraphTopicDto> getGraphTopicDtosWithOwnedNeighbors(List<Topic> topics) {
    List<GraphTopicDto> mapped =
        mappingRegistry.mapListWithAdditionalData(topics, GraphTopicDto.class);
    Set<GraphTopicDto> uniqueMapped = new HashSet<>(mapped);
    for (Topic topic : topics) {
      uniqueMapped.addAll(getOwnedNeighborsOfTopic(topic.id));
    }

    return uniqueMapped.stream().toList();
  }
}
