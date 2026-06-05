package de.thi.mynd.progressTracking.rest;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.service.ChallengeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/challenges")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("authorizedUser")
public class ChallengeResource {

  @Inject ChallengeService challengeService;

  @GET
  @Path("/current")
  public ChallengeDto getCurrentChallenge() {
    return challengeService.getCurrentChallenge();
  }

  @POST
  @Path("/current/claim")
  public Response claimReward() {
    return Response.ok(challengeService.claimReward()).build();
  }

  @GET
  @Path("/history")
  public List<ChallengeDto> getChallengeHistory() {
    return challengeService.getChallengeHistory();
  }
}
