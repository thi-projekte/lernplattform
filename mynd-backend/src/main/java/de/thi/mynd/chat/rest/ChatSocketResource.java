package de.thi.mynd.chat.rest;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.chat.request.SocketMessage;
import de.thi.mynd.chat.request.SocketMessageType;
import de.thi.mynd.chat.service.ChatMessageService;
import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.security.ForbiddenException;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import java.util.UUID;

@BearerTokenAuthentication
@WebSocket(path = "/topics/{topicId}/websocket-chat")
public final class ChatSocketResource {

  @Inject WebSocketConnection connection;

  @Inject ChatMessageService chatMessageService;

  @RolesAllowed("authorizedUser")
  @OnTextMessage
  public void onMessage(@Valid SocketMessage msg) {

    if (msg.getType() == SocketMessageType.Ping) {
      return;
    }

    if (msg.getType() == SocketMessageType.ChatMessage && msg instanceof ChatMessageRequest request) {
      UUID topicId = UUID.fromString(connection.pathParam("topicId"));
      ChatMessageDto dto =  chatMessageService.sendMessageToTopic(topicId, request);
      connection.broadcast().sendText(dto);
    }
  }

  @OnError
  String onError(ForbiddenException e) {
    return "Access denied";
  }
}
