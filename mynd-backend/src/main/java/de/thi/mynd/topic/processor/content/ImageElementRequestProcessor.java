package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.ImageElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.ImageElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class ImageElementRequestProcessor
    implements ContentElementRequestProcessor<ImageElementRequest> {

  @Inject ObjectStorageService storageService;

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(
      ImageElementRequest request, FileUpload file) {

    if (file == null) {
      throw new NoFileProvidedException("Image file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid image");
    }

    try {
      ImageElement contentElement = new ImageElement();
      contentElement.title = request.title;
      contentElement.type = ContentType.IMAGE;
      contentElement.s3Key = "";
      contentElement.originalFileName = request.originalFileName;

      contentElementRepository.persist(contentElement);

      contentElement.s3Key =
          storageService.uploadObject(
              contentElement, file.uploadedFile().toFile(), request.originalFileName);

      contentElementRepository.persistAndFlush(contentElement);

      return contentElement;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof ImageElementRequest;
  }

  private boolean isFileTypeValid(FileUpload file) {
    String contentType = file.contentType();
    if (contentType == null) {
      return false;
    }
    return contentType.startsWith("image/");
  }
}
