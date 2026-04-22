package de.thi.mynd.topic.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicAssociationServiceImplTest {

  @Inject TopicAssociationServiceImpl topicAssociationService;

  @InjectMock TopicAssociationRepository topicAssociationRepository;

  @Test
  void testFindOrCreateOwningTopicAssociations_MixedExistingAndNew() {
    // Arrange
    String username = "test-user";
    Topic owningTopic = new Topic();
    owningTopic.id = UUID.randomUUID();

    // Wir wollen zwei Topics assozieren
    UUID existingTopicId = UUID.randomUUID();
    UUID newTopicId = UUID.randomUUID();

    AssociatedEntityRequest req1 = new AssociatedEntityRequest();
    req1.id = existingTopicId;
    AssociatedEntityRequest req2 = new AssociatedEntityRequest();
    req2.id = newTopicId;
    List<AssociatedEntityRequest> requested = List.of(req1, req2);

    Topic foreignTopic = new Topic();
    foreignTopic.id = existingTopicId;

    // Mock: Eines existiert bereits in der DB
    TopicAssociation existingAssoc = new TopicAssociation();
    existingAssoc.foreignTopic = foreignTopic;
    List<TopicAssociation> mockExisting = new ArrayList<>();
    mockExisting.add(existingAssoc);

    when(topicAssociationRepository.findOwningAssociationsByIdsAndUsername(
            eq(owningTopic.id), anyList(), eq(username)))
        .thenReturn(mockExisting);

    // Mock für getEntityManager().getReference()
    EntityManager mockEm = mock(EntityManager.class);
    when(topicAssociationRepository.getEntityManager()).thenReturn(mockEm);
    Topic mockForeignTopic = new Topic();
    when(mockEm.getReference(eq(Topic.class), eq(newTopicId))).thenReturn(mockForeignTopic);

    // Act
    List<TopicAssociation> result =
        topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            owningTopic, requested, username);

    // Assert
    // Ergebnis sollte 2 Assoziationen enthalten (1 alte, 1 neue)
    Assertions.assertEquals(2, result.size());

    // Verifiziere, dass nur für die NEUE ID persist aufgerufen wurde
    verify(topicAssociationRepository, times(1)).persist(any(TopicAssociation.class));
    verify(mockEm).getReference(Topic.class, newTopicId);

    TopicAssociation newAssoc =
        result.stream()
            .filter(a -> a.creatorId != null && a.creatorId.equals(username))
            .findFirst()
            .orElseThrow();

    Assertions.assertEquals(owningTopic, newAssoc.owningTopic);
    Assertions.assertEquals(username, newAssoc.creatorId);
  }

  @Test
  void testFindOrCreate_AllExisting() {
    // Arrange
    String username = "test-user";
    Topic owningTopic = new Topic();
    owningTopic.id = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    TopicAssociation existing = new TopicAssociation();
    existing.id = id;

    when(topicAssociationRepository.findOwningAssociationsByIdsAndUsername(any(), any(), any()))
        .thenReturn(new ArrayList<>(List.of(existing)));

    AssociatedEntityRequest req = new AssociatedEntityRequest();
    req.id = id;

    // Act
    List<TopicAssociation> result =
        topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            owningTopic, List.of(req), username);

    // Assert
    Assertions.assertEquals(1, result.size());
    verify(topicAssociationRepository, never()).persist(any(TopicAssociation.class));
  }
}
