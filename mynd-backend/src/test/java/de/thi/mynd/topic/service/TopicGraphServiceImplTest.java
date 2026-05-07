package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicGraphRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicGraphServiceImplTest {

  @Inject TopicGraphServiceImpl topicGraphService;

  @InjectMock TopicGraphRepository topicRepository;

  @InjectMock MappingRegistry mappingRegistry;

  private Topic testTopic;
  private GraphTopicDto testDto;
  private UUID topicId;

  @BeforeEach
  void setup() {
    topicId = UUID.randomUUID();
    testTopic = new Topic(); // Assume setters exist or use a constructor
    testDto = GraphTopicDto.builder().build();
  }

  @Test
  void testGetNMostPopularTopicsInGraphAndTheirDirectNeighbors() {
    // Arrange
    int n = 5;
    List<Topic> mockTopics = List.of(testTopic);
    when(topicRepository.findNMostPopular(n)).thenReturn(mockTopics);
    when(mappingRegistry.mapList(mockTopics, GraphTopicDto.class)).thenReturn(List.of(testDto));

    // Act
    List<GraphTopicDto> result =
        topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(n);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(topicRepository, times(1)).findNMostPopular(n);
    verify(mappingRegistry, times(1)).mapList(anyList(), eq(GraphTopicDto.class));
  }

  @Test
  void testGetNMostPopularTopicsWithCategoryFilter() {
    // Arrange
    int n = 3;
    List<UUID> categoryIds = List.of(UUID.randomUUID());
    List<Topic> mockTopics = List.of(testTopic);

    when(topicRepository.findNMostPopularFilterByCategoryIds(n, categoryIds))
        .thenReturn(mockTopics);
    when(mappingRegistry.mapList(mockTopics, GraphTopicDto.class)).thenReturn(List.of(testDto));

    // Act
    List<GraphTopicDto> result =
        topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(n, categoryIds);

    // Assert
    assertEquals(1, result.size());
    verify(topicRepository).findNMostPopularFilterByCategoryIds(n, categoryIds);
  }
}
