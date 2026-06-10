package de.thi.mynd.chat.rest;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.service.ChatMessageService;
import de.thi.mynd.common.dto.PaginationDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

@RolesAllowed("authorizedUser")
@Tag(name = "Topic Chat")
@SecurityRequirement(name = "keycloak")
public final class ChatMessageResource {

  @Inject ChatMessageService chatMessageService;

  @GET
  @Path("/topics/{topicId}/chat")
  @Operation(
      summary = "Gets chat messages for a specific topic paginated",
      description = "Gets chat messages for a specific topic paginated")
  public PaginationDto<ChatMessageDto> getChatMessages(
      UUID topicId, @RestQuery int page, @RestQuery int pageSize) {
    return chatMessageService.getMessages(topicId, page, pageSize);
  }
}
