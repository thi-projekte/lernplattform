package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.RtfElement;
import de.thi.mynd.topic.entity.SpotifyLinkElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.SpotifyLinkElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;

@ApplicationScoped
public final class SpotifyLinkElementRequestProcessor implements ContentElementRequestProcessor<SpotifyLinkElementRequest> {

    @Inject
    ContentElementRepository contentElementRepository;

    @Override
    public ContentElement creteContentElementFromRequest(SpotifyLinkElementRequest request, File file) {
        SpotifyLinkElement contentElement = new SpotifyLinkElement();
        contentElement.title = request.title;
        contentElement.type = ContentType.SpotifyLink;
        contentElement.uri = request.uri;
        contentElementRepository.persistAndFlush(contentElement);

        return contentElement;
    }

    @Override
    public boolean supports(ContentElementRequest request) {
        return request instanceof SpotifyLinkElementRequest;
    }
}
