package de.thi.mynd.topic.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.requests.AssociatedContentElementRequest;
import de.thi.mynd.topic.requests.CreateTopicRequest;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TopicServiceImplTest {

  @Inject TopicServiceImpl topicService;

  @InjectMock TopicRepository topicRepository;

  @InjectMock SecurityIdentity securityIdentity;

  @InjectMock CategoryService categoryService;

  @InjectMock ContentElementService contentElementService;

  @InjectMock TopicAssociationService topicAssociationService;

  @InjectMock MappingRegistry mappingRegistry;

  private static final String USERNAME = "test-user";

  @BeforeEach
  void setup() {
    Principal mockPrincipal = mock(Principal.class);
    when(mockPrincipal.getName()).thenReturn(USERNAME);
    when(securityIdentity.getPrincipal()).thenReturn(mockPrincipal);
  }

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

    CreateTopicRequest request = new CreateTopicRequest();
    request.title = "New Topic";
    request.categories = List.of(category);

    UUID stayId = UUID.randomUUID();
    UUID deleteId = UUID.randomUUID();

    AssociatedContentElementRequest stayReq = new AssociatedContentElementRequest();
    stayReq.id = stayId;
    request.contentElements = List.of(stayReq);

    Topic topicEntity = new Topic();
    topicEntity.contentElements = new ArrayList<>();
    ContentElement oldEl = new ContentElement() {};
    oldEl.id = stayId;
    ContentElement delEl = new ContentElement() {};
    delEl.id = deleteId;
    topicEntity.contentElements.addAll(List.of(oldEl, delEl));

    when(categoryService.findByAssociatedEntities(any())).thenReturn(new ArrayList<>());
    when(topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            any(), any(), anyString()))
        .thenReturn(new ArrayList<>());
    when(mappingRegistry.map(any(), eq(TopicDto.class))).thenReturn(TopicDto.builder().build());

    // Act
    topicService.createTopic(request);

    // Assert
    verify(contentElementService).deleteContentElement(deleteId);
    verify(contentElementService, never()).deleteContentElement(stayId);
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
}
