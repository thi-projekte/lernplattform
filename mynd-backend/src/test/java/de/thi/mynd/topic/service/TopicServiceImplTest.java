package de.thi.mynd.topic.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.dto.TopicWithOwnedRelatedTopicsDto;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.PdfElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.requests.AssociatedContentElementRequest;
import de.thi.mynd.topic.requests.TopicRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TopicServiceImplTest {

  @Inject TopicServiceImpl topicService;

  @InjectMock TopicRepository topicRepository;

  @InjectMock CategoryService categoryService;

  @InjectMock ContentElementService contentElementService;

  @InjectMock TopicAssociationService topicAssociationService;

  @InjectMock MappingRegistry mappingRegistry;

  private static final String USERNAME = "test-user";

  @Test
  void testFindPersonalTopicsPaginated() {
    Topic topic = new Topic();
    topic.title = "My Topic";
    PaginationDto<Topic> repoResult =
        PaginationDto.<Topic>builder().results(List.of(topic)).totalPages(1).build();

    ListTopicDto dto = ListTopicDto.builder().title("My Topic").build();

    when(topicRepository.findForCreatorPaginated(USERNAME, 0, 10)).thenReturn(repoResult);
    when(mappingRegistry.mapList(anyList(), eq(ListTopicDto.class))).thenReturn(List.of(dto));

    // Act
    PaginationDto<ListTopicDto> result = topicService.findPersonalTopicsPaginated(0, 10);

    // Assert
    Assertions.assertEquals(1, result.results.size());
    Assertions.assertEquals("My Topic", result.results.get(0).title);
    verify(topicRepository).findForCreatorPaginated(USERNAME, 0, 10);
  }

  @Test
  void testCreateTopic_WithFullOrchestration() {
    // Arrange

    AssociatedEntityRequest category = new AssociatedEntityRequest();
    category.id = UUID.randomUUID();

    TopicRequest request = new TopicRequest();
    request.title = "New Topic";
    request.categories = List.of(category);

    UUID stayId = UUID.randomUUID();
    UUID deleteId = UUID.randomUUID();

    AssociatedContentElementRequest stayReq = new AssociatedContentElementRequest();
    stayReq.id = stayId;
    request.contentElements = List.of(stayReq);

    when(categoryService.findByAssociatedEntities(any())).thenReturn(new ArrayList<>());
    when(topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            any(), any(), anyString()))
        .thenReturn(new ArrayList<>());
    when(mappingRegistry.map(any(), eq(TopicDto.class))).thenReturn(TopicDto.builder().build());

    // Act
    topicService.createTopic(request);

    // Assert
    verify(topicRepository).persist(any(Topic.class));
    verify(contentElementService)
        .updateTopicAssociation(any(Topic.class), eq(request.contentElements));
  }

  @Test
  void testFindTopicsBySearchMax5() {
    when(topicRepository.findBySearch("test", 5)).thenReturn(new ArrayList<>());
    topicService.findTopicsBySearchMax5("test");
    verify(topicRepository).findBySearch("test", 5);
  }

  @Test
  void testDeleteTopicWithInvalidId() {
    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.empty());

    Exception ex =
        Assertions.assertThrows(
            EntityInstanceNotFoundException.class,
            () -> {
              topicService.deleteTopic(UUID.randomUUID());
            });

    Assertions.assertEquals("No topic exists for the provided UUID", ex.getMessage());
  }

  @Test
  @TestSecurity(user = "creator")
  void testDeleteTopicWithValidId() {

    UUID contentElementId = UUID.randomUUID();
    ContentElement contentElement = new PdfElement();
    contentElement.id = contentElementId;
    contentElement.title = "Content";
    contentElement.creatorId = "creator";
    contentElement.type = ContentType.PDF;

    UUID topicId = UUID.randomUUID();

    Topic topic = new Topic();
    topic.id = topicId;
    topic.creatorId = "creator";
    topic.contentElements = new ArrayList<>();
    topic.contentElements.add(contentElement);

    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.of(topic));

    Assertions.assertDoesNotThrow(
        () -> {
          topicService.deleteTopic(topicId);
        });

    verify(topicRepository, times(1)).findByIdOptional(topicId);
    verify(contentElementService, times(1)).deleteContentElement(contentElementId);
    verify(topicRepository, times(1)).delete(topic);
  }

  @Test
  void testGetTopicWithValidId() {
    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.of(new Topic()));

    UUID topicId = UUID.randomUUID();

    Assertions.assertDoesNotThrow(
        () -> {
          topicService.getTopic(topicId, false);
        });

    verify(topicRepository, times(1)).findByIdOptional(topicId);
  }

  @Test
  void testGetTopicWithInvalidId() {
    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.empty());

    UUID topicId = UUID.randomUUID();

    Assertions.assertThrows(
        EntityInstanceNotFoundException.class,
        () -> {
          topicService.getTopic(topicId, false);
        });

    verify(topicRepository, times(1)).findByIdOptional(topicId);
  }

  @Test
  @TestSecurity(user = "creator")
  void testUpdateTopic_WithFullOrchestration() {
    // Arrange

    AssociatedEntityRequest category = new AssociatedEntityRequest();
    category.id = UUID.randomUUID();

    TopicRequest request = new TopicRequest();
    request.title = "New Topic";
    request.categories = List.of(category);

    UUID stayId = UUID.randomUUID();

    AssociatedContentElementRequest stayReq = new AssociatedContentElementRequest();
    stayReq.id = stayId;
    request.contentElements = List.of(stayReq);

    Topic topic = new Topic();
    topic.creatorId = "creator";

    when(categoryService.findByAssociatedEntities(any())).thenReturn(new ArrayList<>());
    when(topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            any(), any(), anyString()))
        .thenReturn(new ArrayList<>());
    when(mappingRegistry.map(any(), eq(TopicDto.class))).thenReturn(TopicDto.builder().build());
    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.of(topic));

    UUID topicId = UUID.randomUUID();

    // Act
    Assertions.assertDoesNotThrow(
        () -> {
          topicService.updateTopic(topicId, request);
        });

    // Assert
    verify(topicRepository).persist(any(Topic.class));
    verify(contentElementService)
        .updateTopicAssociation(any(Topic.class), eq(request.contentElements));
  }

  @Test
  void testGetOwnedRelatedTopicsForTopic() {
    when(topicRepository.findByOwningTopicId(any())).thenReturn(List.of());

    topicService.getOwnedRelatedTopicsForTopic(UUID.randomUUID());

    verify(mappingRegistry, times(1)).mapList(any(), eq(ListTopicDto.class));
  }

  @Test
  void testGetTopicMappingBasedOnWithOwnedRelatedTopicsFlag()
      throws EntityInstanceNotFoundException {
    when(topicRepository.findByIdOptional(any())).thenReturn(Optional.of(new Topic()));
    when(mappingRegistry.map(any(), eq(TopicWithOwnedRelatedTopicsDto.class)))
        .thenReturn(TopicWithOwnedRelatedTopicsDto.builder().build());
    when(mappingRegistry.map(any(), eq(TopicDto.class))).thenReturn(TopicDto.builder().build());

    TopicDto topicDto1 = topicService.getTopic(UUID.randomUUID(), true);
    TopicDto topicDto2 = topicService.getTopic(UUID.randomUUID(), false);

    Assertions.assertInstanceOf(TopicWithOwnedRelatedTopicsDto.class, topicDto1);
    Assertions.assertFalse(topicDto2 instanceof TopicWithOwnedRelatedTopicsDto);
  }
}
