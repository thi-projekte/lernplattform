package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.RtfElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.RtfElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class RtfElementRequestProcessor
    implements ContentElementRequestProcessor<RtfElementRequest> {

  @Inject ContentElementRepository contentElementRepository;

  @Override
  public ContentElement creteContentElementFromRequest(RtfElementRequest request, FileUpload file) {

    RtfElement contentElement = new RtfElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.Rtf;
    contentElement.rtfText = request.rtfText;
    contentElementRepository.persistAndFlush(contentElement);

    return contentElement;
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof RtfElementRequest;
  }
}
