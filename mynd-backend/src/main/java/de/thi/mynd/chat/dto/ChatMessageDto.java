package de.thi.mynd.chat.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public final class ChatMessageDto {

  public UUID id;
  public String message;
  public MessageSenderDto sender;
}
