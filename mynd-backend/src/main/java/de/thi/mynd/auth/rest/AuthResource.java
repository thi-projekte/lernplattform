package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.service.AuthService;
import de.thi.mynd.auth.service.UserProfileService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/auth")
@Tag(name = "Authorization")
@SecurityRequirement(name = "keycloak")
public final class AuthResource {

  @Inject AuthService authService;

  @Inject UserProfileService userProfileService;

  @Inject SecurityIdentity identity;

  @GET
  @Path("/check-is-builder")
  @Authenticated
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
  @Authenticated
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
  @Authenticated
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

  @POST
  @Path("/profile-picture")
  @Authenticated
  @Operation(
      summary = "Upload a profile picture",
      description = "Uploads a profile picture for the current user. Replaces any existing one.")
  @APIResponse(responseCode = "201", description = "Profile picture uploaded successfully")
  @APIResponse(responseCode = "413", description = "The file is larger than 5 MB")
  @APIResponse(responseCode = "415", description = "Image has invalid file type")
  @APIResponse(responseCode = "400", description = "No file provided")
  public Response uploadProfilePicture(@RestForm("file") FileUpload file) {
    String username = identity.getPrincipal().getName();
    ProfilePictureDto dto = userProfileService.uploadProfilePicture(username, file);
    return Response.status(201).entity(dto).build();
  }

  @DELETE
  @Path("/profile-picture")
  @Authenticated
  @Operation(
      summary = "Delete the profile picture",
      description = "Deletes the profile picture of the current user.")
  @APIResponse(responseCode = "200", description = "Profile picture deleted successfully")
  @APIResponse(responseCode = "404", description = "The user has no profile picture")
  public Response deleteProfilePicture() {
    String username = identity.getPrincipal().getName();
    userProfileService.deleteProfilePicture(username);
    return Response.ok().build();
  }

  @GET
  @Path("/profile-picture/{username}")
  @Authenticated
  @Operation(
      summary = "Get a profile picture",
      description = "Returns a presigned URL for the profile picture of the given user.")
  @APIResponse(responseCode = "200", description = "Presigned URL returned successfully")
  @APIResponse(responseCode = "404", description = "The user has no profile picture")
  public ProfilePictureDto getProfilePicture(@PathParam("username") String username) {
    return userProfileService.getProfilePicture(username);
  }
}
