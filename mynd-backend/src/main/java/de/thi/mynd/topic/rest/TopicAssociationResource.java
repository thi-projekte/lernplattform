package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.service.TopicAssociationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/topic-associations")
@Authenticated
@RolesAllowed("builder")
public final class TopicAssociationResource {

  @Inject TopicAssociationService associationService;

  @POST
  @Path("/create")
  public UUID createAssociation(
      @RestQuery @NotNull UUID owningId, @RestQuery @NotNull UUID foreignId) {
    return associationService.createAssociation(owningId, foreignId).id;
  }

  @DELETE
  @Path("/{associationId}")
  public Response deleteAssociation(UUID associationId) {
    associationService.deleteAssociation(associationId);
    return Response.ok().build();
  }
}
