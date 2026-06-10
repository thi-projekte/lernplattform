package de.thi.mynd.chat.service;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.common.dto.PaginationDto;

import java.util.UUID;

public interface ChatMessageService {

    void sendMessageToTopic(UUID topicId, ChatMessageRequest request);

    PaginationDto<ChatMessageDto> getMessages(UUID topicId, int page, int pageSize);
}
