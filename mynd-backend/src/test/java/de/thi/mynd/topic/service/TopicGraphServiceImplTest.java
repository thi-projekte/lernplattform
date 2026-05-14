package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.repository.TopicGraphRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicGraphServiceImplTest {

  @Inject TopicGraphServiceImpl topicGraphService;

  @InjectMock TopicGraphRepository topicGraphRepository;

  @InjectMock TopicRepository topicRepository;

  @InjectMock MappingRegistry mappingRegistry;

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
  }

  @Test
  void testGetNMostPopularTopicsInGraphAndTheirDirectNeighbors() {
    // Arrange
    int n = 5;
    List<Topic> mockTopics = List.of(testTopic);
    when(topicGraphRepository.findNMostPopular(n)).thenReturn(mockTopics);
    when(topicRepository.findByIdOptional(topicId)).thenReturn(Optional.ofNullable(testTopic));
    when(mappingRegistry.mapListWithAdditionalData(mockTopics, GraphTopicDto.class))
        .thenReturn(List.of(testDto));

    // Act
    List<GraphTopicDto> result =
        topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(n);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(topicGraphRepository, times(1)).findNMostPopular(n);
  }

  @Test
  void testGetNMostPopularTopicsWithCategoryFilter() {
    // Arrange
    int n = 3;
    List<UUID> categoryIds = List.of(UUID.randomUUID());
    List<Topic> mockTopics = List.of(testTopic);

    when(topicGraphRepository.findNMostPopularFilterByCategoryIds(n, categoryIds))
        .thenReturn(mockTopics);
    when(topicRepository.findByIdOptional(topicId)).thenReturn(Optional.ofNullable(testTopic));
    when(mappingRegistry.mapListWithAdditionalData(mockTopics, GraphTopicDto.class))
        .thenReturn(List.of(testDto));

    // Act
    List<GraphTopicDto> result =
        topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(n, categoryIds);

    // Assert
    assertEquals(1, result.size());
    verify(topicGraphRepository).findNMostPopularFilterByCategoryIds(n, categoryIds);
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
}
