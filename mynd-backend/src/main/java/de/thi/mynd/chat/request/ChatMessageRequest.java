package de.thi.mynd.chat.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;

@RegisterForReflection
@Getter
public final class ChatMessageRequest {

    String message;
}
