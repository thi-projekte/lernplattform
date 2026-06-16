/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.chat.repository.ChatMessageRepository;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.processor.MappingRegistry;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class ChatMessageServiceImplTest {

  @Inject ChatMessageServiceImpl chatMessageService;

  @InjectMock ChatMessageRepository chatMessageRepository;

  @InjectMock MappingRegistry mappingRegistry;

  private UUID topicId;

  @BeforeEach
  void setUp() {
    topicId = UUID.randomUUID();
  }

  // ==========================================
  // Tests for: sendMessageToTopic
  // ==========================================

  @Test
  void testSendMessageToTopic_Success() {
    // Arrange
    ChatMessageRequest request = new ChatMessageRequest();
    request.setMessage("Hello, World!");

    ChatMessageDto expectedDto = ChatMessageDto.builder().build();
    // Assuming ChatMessageDto has a setter or fields you can set for assertions

    // Capture the internal ChatMessage object passed to persistAndFlush
    ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

    // Mocking behavior
    doNothing().when(chatMessageRepository).persistAndFlush(any(ChatMessage.class));
    when(mappingRegistry.map(any(ChatMessage.class), eq(ChatMessageDto.class)))
        .thenReturn(expectedDto);

    // Act
    ChatMessageDto result = chatMessageService.sendMessageToTopic(topicId, request);

    // Assert
    assertNotNull(result);
    assertEquals(expectedDto, result);

    // Verify repository interactions and internal state mapping
    verify(chatMessageRepository, times(1)).persistAndFlush(messageCaptor.capture());
    ChatMessage savedMessage = messageCaptor.getValue();
    assertEquals(topicId, savedMessage.topicId);
    assertEquals("Hello, World!", savedMessage.message);

    // Verify mapper interaction
    verify(mappingRegistry, times(1)).map(savedMessage, ChatMessageDto.class);
  }

  // ==========================================
  // Tests for: getMessages
  // ==========================================

  @Test
  void testGetMessages_Success() {
    // Arrange
    int page = 0;
    int pageSize = 10;

    ChatMessage mockMessage = new ChatMessage();
    mockMessage.topicId = topicId;
    mockMessage.message = "Test message";

    PaginationDto<ChatMessage> repositoryPaginationResult =
        PaginationDto.<ChatMessage>builder().results(List.of(mockMessage)).totalPages(5).build();

    ChatMessageDto mappedDto = ChatMessageDto.builder().build();
    List<ChatMessageDto> expectedMappedList = List.of(mappedDto);

    // Mocking repo and mapper
    when(chatMessageRepository.getChatMessagesPaginated(topicId, page, pageSize))
        .thenReturn(repositoryPaginationResult);
    when(mappingRegistry.mapList(repositoryPaginationResult.results, ChatMessageDto.class))
        .thenReturn(expectedMappedList);

    // Act
    PaginationDto<ChatMessageDto> result = chatMessageService.getMessages(topicId, page, pageSize);

    // Assert
    assertNotNull(result);
    assertEquals(5, result.totalPages);
    assertEquals(1, result.results.size());
    assertEquals(expectedMappedList, result.results);

    // Verify proper methods were called
    verify(chatMessageRepository, times(1)).getChatMessagesPaginated(topicId, page, pageSize);
    verify(mappingRegistry, times(1))
        .mapList(repositoryPaginationResult.results, ChatMessageDto.class);
  }

  @Test
  void testGetMessages_EmptyResults() {
    // Arrange
    int page = 1;
    int pageSize = 20;

    PaginationDto<ChatMessage> emptyPaginationResult =
        PaginationDto.<ChatMessage>builder().results(Collections.emptyList()).totalPages(0).build();

    when(chatMessageRepository.getChatMessagesPaginated(topicId, page, pageSize))
        .thenReturn(emptyPaginationResult);
    when(mappingRegistry.mapList(anyList(), eq(ChatMessageDto.class)))
        .thenReturn(Collections.emptyList());

    // Act
    PaginationDto<ChatMessageDto> result = chatMessageService.getMessages(topicId, page, pageSize);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.totalPages);
    java.util.Objects.requireNonNull(result.results);
    assertEquals(0, result.results.size());
  }
}
