/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.request.IndexCardRequest;
import de.thi.mynd.topic.service.IndexCardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/index-cards")
@RolesAllowed("authorizedUser")
@Tag(name = "Index Cards")
@SecurityRequirement(name = "keycloak")
public final class IndexCardResource {

  @Inject IndexCardService indexCardService;

  @POST
  @RolesAllowed("builder")
  @Operation(
      summary = "Creates a new index card",
      description = "Creates a new index card without any topic association")
  @APIResponse(
      responseCode = "201",
      description = "Index card created",
      content = @Content(schema = @Schema(implementation = IndexCardDto.class)))
  public IndexCardDto createContentElement(@Valid IndexCardRequest request) {
    return indexCardService.createIndexCard(request);
  }

  @DELETE
  @RolesAllowed("builder")
  @Path("/{elementId}")
  @Operation(
      summary = "Deletes an index card",
      description =
          "Deletes the index card associated with the given ID and detaches it from the topic.")
  @Parameter(name = "elementId", description = "The unique ID of the index card", required = true)
  @APIResponse(responseCode = "200", description = "Successfully deleted index card")
  @APIResponse(responseCode = "404", description = "No index card found associated with that ID")
  public Response deleteContentElement(UUID elementId) {

    indexCardService.deleteIndexCard(elementId);
    return Response.ok().build();
  }
}
