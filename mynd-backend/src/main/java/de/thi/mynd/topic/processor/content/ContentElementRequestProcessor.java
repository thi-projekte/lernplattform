package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface ContentElementRequestProcessor<R extends ContentElementRequest> {

  ContentElement creteContentElementFromRequest(R request, FileUpload file);

  boolean supports(ContentElementRequest request);
}
