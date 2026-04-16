package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.service.FileAssociatedEntity;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.*;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.processor.content.ContentElementProcessorManager;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.io.File;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ContentElementServiceImpl implements ContentElementService {

  @Inject ContentElementProcessorManager contentElementProcessorManager;

  @Inject ContentElementRepository contentElementRepository;

  @Inject ObjectStorageService objectStorageService;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public ContentElementDto createContentElement(ContentElementRequest request, File file) {
    ContentElement contentElement =
        contentElementProcessorManager.createContentElementFromRequest(request, file);
    return mappingRegistry.map(contentElement, getContentElementDtoClass(contentElement));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ContentElementDto> getContentElementsForTopic(UUID topicId) {
    List<ContentElement> elements = contentElementRepository.findForTopic(topicId);

    return (List<ContentElementDto>)
        mappingRegistry.mapList(elements, this::getContentElementDtoClass);
  }

  @Override
  public void deleteContentElement(UUID elementId) {
    ContentElement element = contentElementRepository.findById(elementId);
    if (element == null) {
      throw new NotFoundException("Content element does not exist");
    }

    if (element instanceof FileAssociatedEntity fileAssociatedEntity) {
      for (String objectKey : fileAssociatedEntity.getFileKeys()) {
        objectStorageService.tryDeleteObject(objectKey);
      }
    }

    contentElementRepository.delete(element);
  }

  private Class<? extends ContentElementDto> getContentElementDtoClass(
      ContentElement contentElement) {
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
