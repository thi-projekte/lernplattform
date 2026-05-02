package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.YouTubeLinkElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.YouTubeLinkElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class YouTubeLinkElementRequestProcessor
    implements ContentElementRequestProcessor<YouTubeLinkElementRequest> {

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(
      YouTubeLinkElementRequest request, FileUpload file) {
    YouTubeLinkElement contentElement = new YouTubeLinkElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.YOUTUBE_LINK;
    contentElement.uri = request.uri;
    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created YouTube content element with id: %s", contentElement.id);

    return contentElement;
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof YouTubeLinkElementRequest;
  }
}
