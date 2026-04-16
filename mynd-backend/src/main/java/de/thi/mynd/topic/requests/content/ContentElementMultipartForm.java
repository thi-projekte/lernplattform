package de.thi.mynd.topic.requests.content;

import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;

public final class ContentElementMultipartForm {

    @RestForm("metadata")
    @PartType(MediaType.APPLICATION_JSON)
    public ContentElementRequest contentElementRequest;

    @RestForm("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public File file;
}
