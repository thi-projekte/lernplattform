package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.topic.dto.content.*;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.processor.content.ContentElementProcessorManager;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;

@ApplicationScoped
public class ContentElementServiceImpl implements ContentElementService {

    @Inject
    ContentElementProcessorManager contentElementProcessorManager;

    @Inject
    MappingRegistry mappingRegistry;


    @Override
    public ContentElementDto createContentElement(ContentElementRequest request, File file) {
        ContentElement contentElement = contentElementProcessorManager.createContentElementFromRequest(request, file);
        return mappingRegistry.map(contentElement, getContentElementDtoClass(contentElement));
    }


    private Class<? extends ContentElementDto> getContentElementDtoClass(ContentElement contentElement) {
        return switch (contentElement) {
            case AudioFileElement a -> AudioFileElementDto.class;
            case ImageElement i -> ImageElementDto.class;
            case PdfElement p -> PdfElementDto.class;
            case RtfElement r -> RtfElementDto.class;
            case SpotifyLinkElement s -> SpotifyLinkElementDto.class;
            case UriElement u -> UriElementDto.class;
            case VideoFileElement v -> VideoFileElementDto.class;
            case YouTubeLinkElement y -> YouTubeLinkElementDto.class;
            default -> ContentElementDto.class;
        };
    }
}
