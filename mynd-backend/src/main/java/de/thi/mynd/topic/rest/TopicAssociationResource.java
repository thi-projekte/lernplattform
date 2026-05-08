package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.CreateTopicAssociationRequest;
import de.thi.mynd.topic.service.TopicAssociationService;
import io.quarkus.security.Authenticated;
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

@Path("/topic-associations")
@Authenticated
public final class TopicAssociationResource {

  @Inject TopicAssociationService associationService;

  @POST
  @RolesAllowed("builder")
  @Produces(MediaType.APPLICATION_JSON)
  public UUID createAssociation(@Valid CreateTopicAssociationRequest request) {
    return associationService.createAssociation(request.owningTopicId, request.foreignTopicId).id;
  }

  @DELETE
  @Path("/{associationId}")
  @RolesAllowed("builder")
  public Response deleteAssociation(UUID associationId) {
    associationService.deleteAssociation(associationId);
    return Response.ok().build();
  }
}
