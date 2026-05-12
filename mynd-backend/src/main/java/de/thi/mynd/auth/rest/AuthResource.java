package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.service.AuthService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@Tag(name = "Authorization")
@SecurityRequirement(name = "keycloak")
public final class AuthResource {

  @Inject AuthService authService;

  @Inject SecurityIdentity identity;

  @GET
  @Path("/check-is-builder")
  @Authenticated
  @Operation(summary = "Check if the user is builder", description = "Checks whether the current user has the builder role assigned.")
  @APIResponse(responseCode = "200", description = "The current user has the builder role")
  @APIResponse(responseCode = "204", description = "The current user does not have the builder role")
  public Response checkUserIsBuilder() {
    String username = identity.getPrincipal().getName();
    if (authService.checkUserIsBuilder(username)) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @POST
  @Path("/register-as-builder")
  @Authenticated
  @Operation(summary = "Register the current user as builder", description = "Assigns the current user the builder role")
  @APIResponse(responseCode = "201", description = "The role got assigned successfully")
  @APIResponse(responseCode = "404", description = "The user could not be found in keycloak")
  public Response makeUserBuilder() {
    String username = identity.getPrincipal().getName();
    authService.makeUserABuilder(username);
    return Response.status(201).build();
  }

  @POST
  @Path("/register-as-learner")
  @Authenticated
  @Operation(summary = "Register the current user as learner", description = "Assigns the current user the learner role")
  @APIResponse(responseCode = "201", description = "The role got assigned successfully")
  @APIResponse(responseCode = "404", description = "The user could not be found in keycloak")
  public Response makeUserLearner() {
    String username = identity.getPrincipal().getName();
    authService.makeUserALearner(username);
    return Response.status(201).build();
  }
}
