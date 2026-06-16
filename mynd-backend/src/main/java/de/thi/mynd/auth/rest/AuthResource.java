/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.service.AuthService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@RolesAllowed("authorizedUser")
@Tag(name = "Authorization")
@SecurityRequirement(name = "keycloak")
public final class AuthResource {

  @Inject AuthService authService;

  @Inject SecurityIdentity identity;

  @GET
  @Path("/check-is-builder")
  @Operation(
      summary = "Check if the user is builder",
      description = "Checks whether the current user has the builder role assigned.")
  @APIResponse(responseCode = "200", description = "The current user has the builder role")
  @APIResponse(
      responseCode = "204",
      description = "The current user does not have the builder role")
  public Response checkUserIsBuilder() {
    String username = identity.getPrincipal().getName();
    if (authService.checkUserIsBuilder(username)) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @POST
  @Path("/register-as-builder")
  @Operation(
      summary = "Register the current user as builder",
      description = "Assigns the current user the builder role")
  @APIResponse(responseCode = "201", description = "The role got assigned successfully")
  @APIResponse(responseCode = "404", description = "The user could not be found in keycloak")
  public Response makeUserBuilder() {
    String username = identity.getPrincipal().getName();
    authService.makeUserABuilder(username);
    return Response.status(201).build();
  }

  @POST
  @Path("/register-as-learner")
  @Operation(
      summary = "Register the current user as learner",
      description = "Assigns the current user the learner role")
  @APIResponse(responseCode = "201", description = "The role got assigned successfully")
  @APIResponse(responseCode = "404", description = "The user could not be found in keycloak")
  public Response makeUserLearner() {
    String username = identity.getPrincipal().getName();
    authService.makeUserALearner(username);
    return Response.status(201).build();
  }
}
