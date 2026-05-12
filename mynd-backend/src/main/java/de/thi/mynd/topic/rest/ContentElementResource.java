package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.service.ContentElementService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/content-elements")
@Tag(name = "Content Elements")
@SecurityRequirement(name = "keycloak")
public final class ContentElementResource {

  @Inject ContentElementService contentElementService;

  @POST
  @RolesAllowed("builder")
  @Operation(summary = "Creates a new content element", description = "Creates a new content element without any topic association")
  @APIResponse(responseCode = "400", description = "If a file is required but not provided")
  @APIResponse(responseCode = "413", description = "The uploaded file is too big for its type")
  @APIResponse(responseCode = "415", description = "The uploaded file has an invalid media type that is not allowed for this content element type")
  public ContentElementDto createContentElement(
      @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid ContentElementRequest request,
      @RestForm("file") FileUpload fileUpload) {
    return contentElementService.createContentElement(request, fileUpload);
  }

  @DELETE
  @RolesAllowed("builder")
  @Path("/{elementId}")
  @Operation(summary = "Deletes an content element", description = "Deletes the content element associated with the given ID and detaches it from the topic.")
  @APIResponse(responseCode = "404", description = "No topic found associated with that ID")
  public Response deleteContentElement(@Parameter(description = "The unique ID of the content element", required = true) UUID elementId) {
    contentElementService.deleteContentElement(elementId);
    return Response.ok().build();
  }
}
