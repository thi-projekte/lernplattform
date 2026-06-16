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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.entity.IndexCard;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.IndexCardRepository;
import de.thi.mynd.topic.request.IndexCardRequest;
import de.thi.mynd.topic.security.IndexCardVoter;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IndexCardServiceImplTest {

  @Inject IndexCardServiceImpl indexCardService;

  @InjectMock IndexCardRepository indexCardRepository;

  @InjectMock MappingRegistry mappingRegistry;

  @InjectMock SecurityService securityService;

  private UUID sampleId;
  private IndexCard sampleCard;
  private IndexCardDto sampleDto;

  @BeforeEach
  void setUp() {
    sampleId = UUID.randomUUID();

    sampleCard = new IndexCard();
    sampleCard.id = sampleId;
    sampleCard.question = "What is Quarkus?";
    sampleCard.answer = "A supersonic subatomic Java framework.";

    sampleDto = IndexCardDto.builder().build();
    // Assuming your DTO mirrors these fields
  }

  // ==========================================
  // TESTS FOR: getIndexCardsForTopic
  // ==========================================
  @Test
  void testGetIndexCardsForTopic_ReturnsMappedDtos() {
    UUID topicId = UUID.randomUUID();
    List<IndexCard> mockCards = List.of(sampleCard);
    List<IndexCardDto> mockDtos = List.of(sampleDto);

    when(indexCardRepository.findByTopicId(topicId)).thenReturn(mockCards);
    when(mappingRegistry.mapList(mockCards, IndexCardDto.class)).thenReturn(mockDtos);

    List<IndexCardDto> result = indexCardService.getIndexCardsForTopic(topicId);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(indexCardRepository).findByTopicId(topicId);
    verify(mappingRegistry).mapList(mockCards, IndexCardDto.class);
  }

  // ==========================================
  // TESTS FOR: createIndexCard
  // ==========================================
  @Test
  void testCreateIndexCard_PersistsAndReturnsDto() {
    IndexCardRequest request = new IndexCardRequest();
    request.question = "What is Quarkus?";
    request.answer = "A supersonic subatomic Java framework.";

    when(mappingRegistry.map(any(IndexCard.class), eq(IndexCardDto.class))).thenReturn(sampleDto);

    IndexCardDto result = indexCardService.createIndexCard(request);

    assertNotNull(result);
    verify(indexCardRepository).persistAndFlush(any(IndexCard.class));
    verify(mappingRegistry).map(any(IndexCard.class), eq(IndexCardDto.class));
  }

  // ==========================================
  // TESTS FOR: deleteIndexCard
  // ==========================================
  @Test
  void testDeleteIndexCard_Success() {
    when(indexCardRepository.findByIdOptional(sampleId)).thenReturn(Optional.of(sampleCard));
    doNothing().when(securityService).denyUnlessGranted(sampleCard, IndexCardVoter.Delete);

    assertDoesNotThrow(() -> indexCardService.deleteIndexCard(sampleId));

    verify(indexCardRepository).delete(sampleCard);
    verify(indexCardRepository).flush();
    verify(securityService).denyUnlessGranted(sampleCard, IndexCardVoter.Delete);
  }

  @Test
  void testDeleteIndexCard_ThrowsException_WhenCardNotFound() {
    when(indexCardRepository.findByIdOptional(sampleId)).thenReturn(Optional.empty());

    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> {
          indexCardService.deleteIndexCard(sampleId);
        });

    verify(indexCardRepository, never()).delete(any());
    verify(securityService, never()).denyUnlessGranted(any(), any());
  }

  @Test
  void testDeleteIndexCard_PropagatesSecurityException_WhenDenied() {
    when(indexCardRepository.findByIdOptional(sampleId)).thenReturn(Optional.of(sampleCard));

    // Simulating SecurityService throwing an error if voting fails
    doThrow(new SecurityException("Access Denied"))
        .when(securityService)
        .denyUnlessGranted(sampleCard, IndexCardVoter.Delete);

    assertThrows(SecurityException.class, () -> indexCardService.deleteIndexCard(sampleId));

    verify(indexCardRepository, never()).delete(any());
  }

  // ==========================================
  // TESTS FOR: updateTopicAssociation
  // ==========================================
  @Test
  void testUpdateTopicAssociation_Success() {
    Topic topic = new Topic();
    topic.id = UUID.randomUUID();

    AssociatedEntityRequest reqElement = new AssociatedEntityRequest();
    reqElement.id = sampleId;
    List<AssociatedEntityRequest> elements = List.of(reqElement);

    when(indexCardRepository.findByIdsTypeSafe(List.of(sampleId))).thenReturn(List.of(sampleCard));
    doNothing().when(securityService).denyUnlessGranted(sampleCard, IndexCardVoter.Assign);

    indexCardService.updateTopicAssociation(topic, elements);

    assertEquals(topic, sampleCard.topic);
    verify(indexCardRepository).persistAndFlush(sampleCard);
    verify(indexCardRepository).flush();
    verify(securityService).denyUnlessGranted(sampleCard, IndexCardVoter.Assign);
  }

  @Test
  void testUpdateTopicAssociation_Aborts_WhenSecurityDeniesAnElement() {
    Topic topic = new Topic();
    AssociatedEntityRequest reqElement = new AssociatedEntityRequest();
    reqElement.id = sampleId;

    when(indexCardRepository.findByIdsTypeSafe(any())).thenReturn(List.of(sampleCard));
    doThrow(new SecurityException("Forbidden"))
        .when(securityService)
        .denyUnlessGranted(sampleCard, IndexCardVoter.Assign);

    assertThrows(
        SecurityException.class,
        () -> {
          indexCardService.updateTopicAssociation(topic, List.of(reqElement));
        });

    // The transaction will roll back, ensuring database consistency
    verify(indexCardRepository, never()).persistAndFlush(any());
  }
}
