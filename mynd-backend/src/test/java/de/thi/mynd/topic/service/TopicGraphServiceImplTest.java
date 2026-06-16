/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.service.LearnProgressService;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.repository.TopicGraphRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicGraphServiceImplTest {

  @Inject TopicGraphServiceImpl topicGraphService;

  @InjectMock TopicGraphRepository topicGraphRepository;

  @InjectMock TopicRepository topicRepository;

  @InjectMock MappingRegistry mappingRegistry;

  @InjectMock LearnProgressService learnProgressService;

  @InjectMock SecurityIdentity identity;

  private Topic testTopic;
  private GraphTopicDto testDto;
  private UUID topicId;
  private String creatorId;

  @BeforeEach
  void setup() {
    topicId = UUID.randomUUID();
    testTopic = new Topic(); // Assume setters exist or use a constructor
    testTopic.id = topicId;
    testTopic.foreignAssociations = new ArrayList<>();
    testTopic.ownedAssociations = new ArrayList<>();
    testDto = GraphTopicDto.builder().build();
    creatorId = "user-123";

    Principal mockPrincipal = mock(Principal.class);
    when(mockPrincipal.getName()).thenReturn(creatorId);
    when(identity.getPrincipal()).thenReturn(mockPrincipal);
  }

  @Test
  void testGetNMostPopularTopics() {
    // Arrange
    List<Topic> mockTopics = List.of(testTopic);
    when(topicGraphRepository.findNMostPopular(5, creatorId)).thenReturn(mockTopics);
    when(topicRepository.findByIdOptional(topicId)).thenReturn(Optional.ofNullable(testTopic));

    // Act
    // This assumes getGraphTopicDtosWithNeighbors is an internal method
    // that likely calls mappingRegistry or other repo methods.
    var result =
        topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(5, creatorId);

    // Assert
    verify(topicGraphRepository).findNMostPopular(5, creatorId);
    assertNotNull(result);
  }

  @Test
  void testGetNMostPopularTopicsWithCategoryFilterCreatorFilter() {
    // Arrange
    List<UUID> categories = List.of(UUID.randomUUID());
    when(topicGraphRepository.findNMostPopularFilterByCategoryIds(anyInt(), anyList(), anyString()))
        .thenReturn(Collections.emptyList());
    when(topicRepository.findByIdOptional(topicId)).thenReturn(Optional.ofNullable(testTopic));

    // Act
    topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(5, categories, creatorId);

    // Assert
    verify(topicGraphRepository).findNMostPopularFilterByCategoryIds(5, categories, creatorId);
  }

  @Test
  void testGetOwnedNeighborsOfTopic_Success() throws EntityInstanceNotFoundException {
    // Arrange
    Topic mainTopic = new Topic();
    Topic neighborTopic = new Topic();
    TopicAssociation association = new TopicAssociation();
    association.foreignTopic = neighborTopic;
    mainTopic.ownedAssociations = List.of(association);

    when(topicRepository.findByIdOptional(topicId)).thenReturn(Optional.of(mainTopic));
    when(mappingRegistry.mapListWithAdditionalData(anyList(), eq(GraphTopicDto.class)))
        .thenReturn(List.of(GraphTopicDto.builder().build()));

    // Act
    List<GraphTopicDto> result = topicGraphService.getOwnedNeighborsOfTopic(topicId);

    // Assert
    assertFalse(result.isEmpty());
    verify(mappingRegistry)
        .mapListWithAdditionalData(
            argThat(list -> list.contains(neighborTopic)), eq(GraphTopicDto.class));
  }

  @Test
  void testGetOwnedNeighborsOfTopic_NotFound() {
    // Arrange
    when(topicRepository.findByIdOptional(topicId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> {
          topicGraphService.getOwnedNeighborsOfTopic(topicId);
        });
  }

  @Test
  void testSearchTopicNodes() {
    // Arrange
    String query = "AI";
    int limit = 10;
    when(topicRepository.findBySearch(query, limit)).thenReturn(Collections.emptyList());
    when(mappingRegistry.mapListWithAdditionalData(anyList(), eq(GraphTopicDto.class)))
        .thenReturn(new ArrayList<>());

    // Act
    topicGraphService.searchTopicNodes(query, limit);

    // Assert
    verify(topicRepository).findBySearch(query, limit);
    verify(mappingRegistry).mapListWithAdditionalData(anyList(), eq(GraphTopicDto.class));
  }

  @Test
  @DisplayName("Should fetch popular topics and map their neighbors when user has no progress")
  void testGetLearnGraph_NoProgress_FetchesNeighbors() {
    // Arrange
    when(learnProgressService.getLastNUncompletedTopicsForUser(10, creatorId))
        .thenReturn(Collections.emptyList());

    // 1. Setup Mock Topics & Relationships
    UUID popularTopicId = UUID.randomUUID();
    Topic popularTopic = mock(Topic.class);
    popularTopic.id = popularTopicId; // Ensure ID matches what the loop extracts

    // Set up associations for the neighbor lookup
    Topic neighborTopic = mock(Topic.class);
    neighborTopic.id = UUID.randomUUID();

    when(popularTopic.ownedAssociations).thenReturn(Collections.emptyList());
    when(popularTopic.foreignAssociations).thenReturn(Collections.emptyList());

    List<Topic> popularTopics = List.of(popularTopic);
    when(topicGraphRepository.findNMostPopular(10)).thenReturn(popularTopics);

    // Mock repository lookup inside getNeighborsOfTopic
    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.of(popularTopic));

    // 2. Mock the mapping registry calls
    GraphTopicDto popularDto = GraphTopicDto.builder().id(UUID.randomUUID()).build();
    GraphTopicDto neighborDto = GraphTopicDto.builder().id(UUID.randomUUID()).build();

    // Mapping for the primary popular topics list
    when(mappingRegistry.mapListWithAdditionalData(popularTopics, GraphTopicDto.class))
        .thenReturn(List.of(popularDto));
    // Mapping for the neighbors list
    when(mappingRegistry.mapListWithAdditionalData(List.of(neighborTopic), GraphTopicDto.class))
        .thenReturn(List.of(neighborDto));

    // Act
    List<GraphTopicDto> result = topicGraphService.getLearnGraph();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.contains(popularDto));
  }

  @Test
  @DisplayName(
      "Should throw exception or bubble up if a topic's neighbor lookup finds a missing topic ID")
  void testGetLearnGraph_NeighborTopicNotFound_ThrowsException() {
    // Arrange
    when(learnProgressService.getLastNUncompletedTopicsForUser(10, creatorId))
        .thenReturn(Collections.emptyList());

    UUID missingTopicId = UUID.randomUUID();
    Topic targetTopic = mock(Topic.class);
    targetTopic.id = missingTopicId;

    when(topicGraphRepository.findNMostPopular(10)).thenReturn(List.of(targetTopic));

    // Simulate database returning empty for the topic lookup inside getNeighborsOfTopic
    when(topicRepository.findByIdOptional(missingTopicId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> {
          topicGraphService.getLearnGraph();
        });
  }
}
