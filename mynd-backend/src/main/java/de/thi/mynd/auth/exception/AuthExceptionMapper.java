package de.thi.mynd.auth.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public final class AuthExceptionMapper {

  @ServerExceptionMapper
  public Response mapProfilePictureNotFoundException(ProfilePictureNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapNoInvitationsLeftException(NoInvitationsLeftException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapCannotAcceptInvitationException(CannotAcceptInvitationException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }
}
