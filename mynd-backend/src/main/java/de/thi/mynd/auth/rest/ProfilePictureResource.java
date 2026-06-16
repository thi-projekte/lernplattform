/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.service.UserProfileService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
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
@RolesAllowed("authorizedUser")
@Tag(name = "Profile Picture")
@SecurityRequirement(name = "keycloak")
public final class ProfilePictureResource {

  @Inject UserProfileService userProfileService;

  @Inject SecurityIdentity identity;

  @POST
  @Path("/profile-picture")
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
  @Operation(
      summary = "Get a profile picture",
      description = "Returns a presigned URL for the profile picture of the given user.")
  @APIResponse(responseCode = "200", description = "Presigned URL returned successfully")
  @APIResponse(responseCode = "404", description = "The user has no profile picture")
  public ProfilePictureDto getProfilePicture(@PathParam("username") String username) {
    return userProfileService.getProfilePicture(username);
  }
}
