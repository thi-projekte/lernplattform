/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.progressTracking.rest;

import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.request.TopicNoteRequest;
import de.thi.mynd.progressTracking.service.TopicNoteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/topic-notes")
@RolesAllowed("authorizedUser")
@Tag(name = "Topic Notes")
@SecurityRequirement(name = "keycloak")
public final class TopicNoteResource {

  @Inject TopicNoteService topicNoteService;

  @GET
  @Path("/{topicId}")
  @Operation(summary = "Gets the personal topic notes for a specific topic")
  public TopicNoteDto getTopicNote(UUID topicId) {
    return topicNoteService.getTopicNoteForCurrentUser(topicId);
  }

  @PUT
  @Path("/{topicId}")
  @Operation(summary = "Updates the personal topic notes for a specific topic")
  public TopicNoteDto updateTopicNote(UUID topicId, @Valid TopicNoteRequest request) {
    return topicNoteService.updateTopicNoteForCurrentUser(topicId, request);
  }
}
