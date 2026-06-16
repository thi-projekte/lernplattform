/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.rest;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.request.StreakPreferenceRequest;
import de.thi.mynd.progressTracking.service.StreakService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/streaks")
@RolesAllowed("authorizedUser")
@Tag(name = "Streaks")
@SecurityRequirement(name = "keycloak")
public final class StreakResource {

  @Inject StreakService streakService;

  @GET
  @Operation(
      summary = "Get active streaks for current user",
      description =
          "Returns all non-ended streaks for the authenticated user. "
              + "Streaks that are no longer active are automatically ended before returning.")
  @APIResponse(
      responseCode = "200",
      description = "Streaks returned successfully",
      content = @Content(schema = @Schema(implementation = StreakDto.class)))
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public List<StreakDto> getStreaks() {
    return streakService.getLatestStreaksForCurrentUser();
  }

  @GET
  @Path("/preferences")
  @Operation(
      summary = "Get streak preferences for current user",
      description =
          "Returns the streak display preferences for the authenticated user. "
              + "Creates a default preference if none exists yet.")
  @APIResponse(
      responseCode = "200",
      description = "Preferences returned successfully",
      content = @Content(schema = @Schema(implementation = StreakPreferenceDto.class)))
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public StreakPreferenceDto getStreakPreferences() {
    return streakService.getOrCreateStreakPreferenceForCurrentUser();
  }

  @PUT
  @Path("/preferences")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Update streak preferences for current user",
      description =
          "Updates the streak display preferences (type and visibility) "
              + "for the authenticated user. Creates the preference record if it does not exist yet.")
  @APIResponse(responseCode = "200", description = "Preferences updated successfully")
  @APIResponse(responseCode = "400", description = "Invalid request body")
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public Response updateStreakPreferences(@Valid @NotNull StreakPreferenceRequest request) {
    streakService.updateStreakPreferencesForCurrentUser(request);
    return Response.ok().build();
  }
}
