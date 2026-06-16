/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.entity.TopicNoteId;
import de.thi.mynd.progressTracking.repository.TopicNoteRepository;
import de.thi.mynd.progressTracking.request.TopicNoteRequest;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicNoteServiceImplTest {

  @Inject TopicNoteServiceImpl topicNoteService;

  @InjectMock TopicNoteRepository topicNoteRepository;

  @InjectMock SecurityIdentity securityIdentity;

  @InjectMock MappingRegistry mappingRegistry;

  private final String MOCK_USER = "test-user-123";
  private final UUID MOCK_TOPIC_ID = UUID.randomUUID();
  private TopicNoteId expectedId;

  @BeforeEach
  void setUp() {
    // Mock the SecurityIdentity to return our test user
    Principal mockPrincipal = mock(Principal.class);
    when(mockPrincipal.getName()).thenReturn(MOCK_USER);
    when(securityIdentity.getPrincipal()).thenReturn(mockPrincipal);

    // Setup the expected composite ID used across methods
    expectedId = new TopicNoteId();
    expectedId.creatorId = MOCK_USER;
    expectedId.topicId = MOCK_TOPIC_ID;
  }

  // ==========================================
  // Tests for getTopicNoteForCurrentUser
  // ==========================================

  @Test
  void getTopicNoteForCurrentUser_WhenNoteExists_ReturnsMappedDto() {
    // Arrange
    TopicNote existingNote = new TopicNote();
    existingNote.id = expectedId;
    existingNote.content = "Existing Content";

    TopicNoteDto expectedDto = TopicNoteDto.builder().build(); // Assuming content gets mapped here

    when(topicNoteRepository.findByIdOptional(any(TopicNoteId.class)))
        .thenReturn(Optional.of(existingNote));
    when(mappingRegistry.map(existingNote, TopicNoteDto.class)).thenReturn(expectedDto);

    // Act
    TopicNoteDto result = topicNoteService.getTopicNoteForCurrentUser(MOCK_TOPIC_ID);

    // Assert
    assertNotNull(result);
    assertEquals(expectedDto, result);
    verify(topicNoteRepository, never()).persistAndFlush(any(TopicNote.class));
  }

  @Test
  void getTopicNoteForCurrentUser_WhenNoteDoesNotExist_CreatesDefaultAndReturnsDto() {
    // Arrange
    TopicNoteDto expectedDto = TopicNoteDto.builder().build();

    when(topicNoteRepository.findByIdOptional(any(TopicNoteId.class))).thenReturn(Optional.empty());
    when(mappingRegistry.map(any(TopicNote.class), eq(TopicNoteDto.class))).thenReturn(expectedDto);

    // Act
    TopicNoteDto result = topicNoteService.getTopicNoteForCurrentUser(MOCK_TOPIC_ID);

    // Assert
    assertNotNull(result);
    assertEquals(expectedDto, result);
    // Verify that a new note was actually created and saved
    verify(topicNoteRepository, times(1)).persistAndFlush(any(TopicNote.class));
  }

  // ==========================================
  // Tests for updateTopicNoteForCurrentUser
  // ==========================================

  @Test
  void updateTopicNoteForCurrentUser_WhenNoteExists_UpdatesContentAndSaves() {
    // Arrange
    TopicNote existingNote = new TopicNote();
    existingNote.id = expectedId;
    existingNote.content = "Old Content";

    TopicNoteRequest request = new TopicNoteRequest();
    request.content = "Updated Content";

    TopicNoteDto expectedDto = TopicNoteDto.builder().build();

    when(topicNoteRepository.findByIdOptional(any(TopicNoteId.class)))
        .thenReturn(Optional.of(existingNote));
    when(mappingRegistry.map(existingNote, TopicNoteDto.class)).thenReturn(expectedDto);

    // Act
    TopicNoteDto result = topicNoteService.updateTopicNoteForCurrentUser(MOCK_TOPIC_ID, request);

    // Assert
    assertNotNull(result);
    assertEquals("Updated Content", existingNote.content); // Verify entity was mutated
    verify(topicNoteRepository, times(1)).persistAndFlush(existingNote);
  }

  // ==========================================
  // Tests for createDefaultForCurrentUser
  // ==========================================

  @Test
  void createDefaultForCurrentUser_SavesAndReturnsEmptyNote() {
    // Act
    TopicNote result = topicNoteService.createDefaultForCurrentUser(MOCK_TOPIC_ID);

    // Assert
    assertNotNull(result);
    assertEquals(MOCK_USER, result.id.creatorId);
    assertEquals(MOCK_TOPIC_ID, result.id.topicId);
    assertEquals("", result.content);
    verify(topicNoteRepository, times(1)).persistAndFlush(result);
  }
}
