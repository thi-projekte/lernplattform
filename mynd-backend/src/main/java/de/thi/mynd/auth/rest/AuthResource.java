package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.service.AuthService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public final class AuthResource {

  @Inject AuthService authService;

  @Inject SecurityIdentity identity;

  @GET
  @Path("/check-is-builder")
  public Response checkUserIsBuilder() {
    String username = identity.getPrincipal().getName();
    if (authService.checkUserIsBuilder(username)) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @POST
  @Path("/register-as-builder")
  public Response makeUserBuilder() {
    String username = identity.getPrincipal().getName();
    authService.makeUserABuilder(username);
    return Response.status(201).build();
  }
}
