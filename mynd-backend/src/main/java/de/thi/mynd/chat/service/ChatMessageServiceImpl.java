/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.chat.service;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.chat.repository.ChatMessageRepository;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.processor.MappingRegistry;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class ChatMessageServiceImpl implements ChatMessageService {

  @Inject ChatMessageRepository chatMessageRepository;

  @Inject MappingRegistry mappingRegistry;

  @Override
  @Transactional
  public ChatMessageDto sendMessageToTopic(UUID topicId, ChatMessageRequest request) {
    ChatMessage message = new ChatMessage();
    message.topicId = topicId;
    message.message = request.getMessage();

    chatMessageRepository.persistAndFlush(message);

    Log.infof("Created new chat message in topic %s", topicId);

    return mappingRegistry.map(message, ChatMessageDto.class);
  }

  @Override
  public PaginationDto<ChatMessageDto> getMessages(UUID topicId, int page, int pageSize) {
    PaginationDto<ChatMessage> messages =
        chatMessageRepository.getChatMessagesPaginated(topicId, page, pageSize);

    List<ChatMessageDto> mapped = mappingRegistry.mapList(messages.results, ChatMessageDto.class);

    Log.tracef("Loading chat messages for topic %s page %d size %d", topicId, page, pageSize);

    return PaginationDto.<ChatMessageDto>builder()
        .results(mapped)
        .totalPages(messages.totalPages)
        .build();
  }
}
