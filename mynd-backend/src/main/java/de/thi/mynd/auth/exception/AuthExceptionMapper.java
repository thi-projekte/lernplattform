/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
