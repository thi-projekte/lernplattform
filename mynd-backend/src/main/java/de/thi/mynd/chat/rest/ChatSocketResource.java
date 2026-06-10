package de.thi.mynd.chat.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.chat.service.ChatMessageService;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@WebSocket(path = "/topics/{topicId}/websocket-chat")
//@RolesAllowed("authorizedUser")
@Tag(name = "Categories")
@SecurityRequirement(name = "keycloak")
public final class ChatSocketResource {

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatMessageService chatMessageService;

    @OnTextMessage(broadcast = true)
    public ChatMessageDto onMessage(ChatMessageRequest request) {

        UUID topicId = UUID.fromString(connection.pathParam("topicId"));
        return chatMessageService.sendMessageToTopic(topicId, request);
    }
}
