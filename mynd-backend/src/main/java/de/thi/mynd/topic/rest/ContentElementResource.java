package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.entity.ContentElement;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/content-elements")
public final class ContentElementResource {

    @POST
    public ContentElement createContentElement(ContentElement contentElement) {

    }
}
