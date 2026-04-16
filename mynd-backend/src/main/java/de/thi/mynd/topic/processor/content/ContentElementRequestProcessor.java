package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.requests.content.ContentElementRequest;

import java.io.File;

public interface ContentElementRequestProcessor<R extends ContentElementRequest> {

    ContentElement creteContentElementFromRequest(R request, File file);

    boolean supports(ContentElementRequest request);
}
