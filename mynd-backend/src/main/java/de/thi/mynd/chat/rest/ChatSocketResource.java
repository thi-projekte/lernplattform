/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.chat.rest;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.chat.request.SocketMessage;
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
  @OnTextMessage(broadcast = true)
  public ChatMessageDto onMessage(@Valid SocketMessage msg) {

    if (msg instanceof ChatMessageRequest request) {
      UUID topicId = UUID.fromString(connection.pathParam("topicId"));
      return chatMessageService.sendMessageToTopic(topicId, request);
    }

    return null;
  }

  @OnError
  String onError(ForbiddenException e) {
    return "Access denied";
  }
}
