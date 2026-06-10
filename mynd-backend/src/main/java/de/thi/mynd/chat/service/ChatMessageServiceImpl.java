package de.thi.mynd.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.chat.repository.ChatMessageRepository;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.processor.MappingRegistry;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public final class ChatMessageServiceImpl implements ChatMessageService {

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    MappingRegistry mappingRegistry;

    @Inject
    ObjectMapper objectMapper;


    @Override
    @Transactional
    public void sendMessageToTopic(UUID topicId, ChatMessageRequest request) {
        ChatMessage message = new ChatMessage();
        message.topicId = topicId;
        message.message = request.getMessage();

        chatMessageRepository.persistAndFlush(message);

        ChatMessageDto dto = mappingRegistry.map(message, ChatMessageDto.class);

        if (connection != null && connection.isOpen()) {
            try {
                connection.broadcast().sendBinary(objectMapper.writeValueAsBytes(dto));
            } catch (JsonProcessingException e) {
                Log.error(e.getMessage());
                throw new RuntimeException("Internal error");
            }
        }
    }

    @Override
    public PaginationDto<ChatMessageDto> getMessages(UUID topicId, int page, int pageSize) {
        return null;
    }
}
