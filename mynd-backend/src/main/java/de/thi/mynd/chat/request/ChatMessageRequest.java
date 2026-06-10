package de.thi.mynd.chat.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@RegisterForReflection
@Getter
public final class ChatMessageRequest {

    @NotBlank String message;
}
