/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.CreateTopicAssociationRequest;
import de.thi.mynd.topic.service.TopicAssociationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/topic-associations")
@RolesAllowed("authorizedUser")
@Tag(name = "Topic Associations")
@SecurityRequirement(name = "keycloak")
public final class TopicAssociationResource {

  @Inject TopicAssociationService associationService;

  @POST
  @RolesAllowed("builder")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Creates a new association between two topics",
      description =
          "Creates a new association between the two given topics. If the association already exists nothing happens.")
  @APIResponse(responseCode = "404", description = "One of the topics does not exist")
  public UUID createAssociation(@Valid CreateTopicAssociationRequest request) {
    return associationService.createAssociation(request.owningTopicId, request.foreignTopicId).id;
  }

  @DELETE
  @Path("/{associationId}")
  @RolesAllowed("builder")
  @Operation(
      summary = "Deletes an association",
      description = "Deletes the association with the given ID")
  @APIResponse(responseCode = "404", description = "The topic association does not exist")
  public Response deleteAssociation(UUID associationId) {
    associationService.deleteAssociation(associationId);
    return Response.ok().build();
  }
}
