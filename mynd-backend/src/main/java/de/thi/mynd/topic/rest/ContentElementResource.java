package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.service.ContentElementService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/content-elements")
public final class ContentElementResource {

    @Inject
    ContentElementService contentElementService;

    @POST
    public ContentElementDto createContentElement(
            @RestForm @PartType(MediaType.APPLICATION_JSON) ContentElementRequest request,
            @RestForm("file") FileUpload fileUpload
            ) {
        return contentElementService.createContentElement(request, fileUpload.uploadedFile().toFile());
    }
}
