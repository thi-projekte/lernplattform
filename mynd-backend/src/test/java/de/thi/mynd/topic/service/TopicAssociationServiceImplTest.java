/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.exception.AssociationAlreadyExistsException;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.security.TopicAssociationVoter;
import de.thi.mynd.topic.security.TopicVoter;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicAssociationServiceImplTest {

  @Inject TopicAssociationServiceImpl topicAssociationService;

  @InjectMock TopicAssociationRepository topicAssociationRepository;

  @InjectMock TopicRepository topicRepository;

  @InjectMock SecurityService securityService;

  private UUID ownerId;
  private UUID foreignId;
  private Topic owner;
  private Topic foreign;

  @BeforeEach
  void setup() {
    ownerId = UUID.randomUUID();
    foreignId = UUID.randomUUID();
    owner = new Topic();
    foreign = new Topic();
  }

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
    assertEquals(2, result.size());

    // Verifiziere, dass nur für die NEUE ID persist aufgerufen wurde
    verify(topicAssociationRepository, times(1)).persist(any(TopicAssociation.class));
    verify(mockEm).getReference(Topic.class, newTopicId);

    TopicAssociation newAssoc =
        result.stream()
            .filter(a -> a.creatorId != null && a.creatorId.equals(username))
            .findFirst()
            .orElseThrow();

    assertEquals(owningTopic, newAssoc.owningTopic);
    assertEquals(username, newAssoc.creatorId);
  }

  @Test
  void testFindOrCreate_AllExisting() {
    // Arrange
    String username = "test-user";
    Topic owningTopic = new Topic();
    owningTopic.id = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    Topic foreignTopic = new Topic();
    foreignTopic.id = id;

    TopicAssociation existing = new TopicAssociation();
    existing.foreignTopic = foreignTopic;

    when(topicAssociationRepository.findOwningAssociationsByIdsAndUsername(any(), any(), any()))
        .thenReturn(new ArrayList<>(List.of(existing)));

    AssociatedEntityRequest req = new AssociatedEntityRequest();
    req.id = id;

    // Act
    List<TopicAssociation> result =
        topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            owningTopic, List.of(req), username);

    // Assert
    assertEquals(1, result.size());
    verify(topicAssociationRepository, never()).persist(any(TopicAssociation.class));
  }

  @Test
  void createAssociation_Success() {
    // Arrange
    when(topicRepository.findByIdOptional(ownerId)).thenReturn(Optional.of(owner));
    when(topicRepository.findByIdOptional(foreignId)).thenReturn(Optional.of(foreign));
    when(topicAssociationRepository.associationExists(owner, foreign)).thenReturn(false);

    // Act
    TopicAssociation result = topicAssociationService.createAssociation(ownerId, foreignId);

    // Assert
    assertNotNull(result);
    assertEquals(owner.id, result.owningTopic.id);
    assertEquals(foreign.id, result.foreignTopic.id);

    verify(securityService).denyUnlessGranted(owner, TopicVoter.AssignForeignTopics);
    verify(topicAssociationRepository).persistAndFlush(any(TopicAssociation.class));
  }

  @Test
  void createAssociation_ThrowsException_WhenAlreadyExists() {
    // Arrange
    when(topicRepository.findByIdOptional(ownerId)).thenReturn(Optional.of(owner));
    when(topicRepository.findByIdOptional(foreignId)).thenReturn(Optional.of(foreign));
    when(topicAssociationRepository.associationExists(owner, foreign)).thenReturn(true);

    // Act & Assert
    assertThrows(
        AssociationAlreadyExistsException.class,
        () -> {
          topicAssociationService.createAssociation(ownerId, foreignId);
        });
  }

  @Test
  void createAssociation_ThrowsException_WhenTopicNotFound() {
    // Arrange
    when(topicRepository.findByIdOptional(ownerId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> {
          topicAssociationService.createAssociation(ownerId, foreignId);
        });
  }

  // --- Tests for deleteAssociation ---

  @Test
  void deleteAssociation_Success() {
    // Arrange
    UUID assocId = UUID.randomUUID();
    TopicAssociation association = new TopicAssociation();
    when(topicAssociationRepository.findByIdOptional(assocId)).thenReturn(Optional.of(association));

    // Act
    topicAssociationService.deleteAssociation(assocId);

    // Assert
    verify(securityService).denyUnlessGranted(association, TopicAssociationVoter.Delete);
    verify(topicAssociationRepository).delete(association);
    verify(topicAssociationRepository).flush();
  }

  @Test
  void deleteAssociation_ThrowsException_WhenNotFound() {
    // Arrange
    UUID assocId = UUID.randomUUID();
    when(topicAssociationRepository.findByIdOptional(assocId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> {
          topicAssociationService.deleteAssociation(assocId);
        });
  }
}
