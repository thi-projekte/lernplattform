package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.service.ContentElementService;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/content-elements")
public final class ContentElementResource {

  @Inject ContentElementService contentElementService;

  @Inject
  SecurityIdentity identity;

  @POST
  public ContentElementDto createContentElement(
      @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid ContentElementRequest request,
      @RestForm("file") FileUpload fileUpload) {
    Log.info(identity.getRoles());
    return contentElementService.createContentElement(request, fileUpload);
  }

  @DELETE
  @Path("/{elementId}")
  public Response deleteContentElement(UUID elementId) {
    contentElementService.deleteContentElement(elementId);
    return Response.ok().build();
  }
}
