package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.service.FileAssociatedEntity;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.*;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.processor.content.ContentElementProcessorManager;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.AssociatedContentElementRequest;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class ContentElementServiceImpl implements ContentElementService {

  @Inject ContentElementProcessorManager contentElementProcessorManager;

  @Inject ContentElementRepository contentElementRepository;

  @Inject ObjectStorageService objectStorageService;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public ContentElementDto createContentElement(ContentElementRequest request, FileUpload file) {
    ContentElement contentElement =
        contentElementProcessorManager.createContentElementFromRequest(request, file);

    Log.infof("Created new content element with id %s and type %s", contentElement.id, contentElement.type.label);

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
  @Transactional
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
    Log.infof("Successfully deleted content element with ID %s", element.id);
  }

  @Override
  @Transactional
  public void updateTopicAssociation(Topic topic, List<AssociatedContentElementRequest> associatedElements) {
    Map<UUID, Integer> ranking = associatedElements.stream()
            .collect(Collectors.toMap(e -> e.id, e -> e.rank));

    List<ContentElement> contentElements = contentElementRepository.findByIdsTypeSafe(ranking.keySet().stream().toList());
    for (ContentElement element : contentElements) {
      element.topic = topic;
      element.rank = ranking.get(element.id);
      contentElementRepository.persist(element);
    }

    contentElementRepository.flush();
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
