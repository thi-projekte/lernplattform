package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.SpotifyLinkElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.SpotifyLinkElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class SpotifyLinkElementRequestProcessor
    implements ContentElementRequestProcessor<SpotifyLinkElementRequest> {

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(
      SpotifyLinkElementRequest request, FileUpload file) {
    SpotifyLinkElement contentElement = new SpotifyLinkElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.SPOTIFY_LINK;
    contentElement.icon = request.icon;
    contentElement.uri = request.uri;
    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created spotify link content element with id: %s", contentElement.id);

    return contentElement;
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof SpotifyLinkElementRequest;
  }
}
