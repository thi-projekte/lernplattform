package de.thi.mynd.chat.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public final class ChatMessageRequest extends SocketMessage {

  @NotBlank String message;
}
