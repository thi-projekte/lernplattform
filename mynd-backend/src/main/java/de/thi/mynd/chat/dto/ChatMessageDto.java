package de.thi.mynd.chat.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public final class ChatMessageDto {

    public UUID id;
    public String message;
    public MessageSenderDto sender;
}
