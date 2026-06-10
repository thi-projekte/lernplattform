package de.thi.mynd.progressTracking.rest;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.service.ChallengeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/challenges")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("authorizedUser")
@Tag(name = "Challenges")
@SecurityRequirement(name = "keycloak")
public final class ChallengeResource {

  @Inject ChallengeService challengeService;

  @GET
  @Path("/current")
  public ChallengeDto getCurrentChallenge() {
    return challengeService.getCurrentChallenge();
  }

  @POST
  @Path("/claim/{id}")
  public Response claimReward(UUID id) {
    return Response.ok(challengeService.claimReward(id)).build();
  }

  @GET
  @Path("/history")
  public List<ChallengeDto> getChallengeHistory() {
    return challengeService.getChallengeHistory();
  }
}
