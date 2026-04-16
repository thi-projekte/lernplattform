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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ApplicationScoped
public final class ImageElementRequestProcessor
    implements ContentElementRequestProcessor<ImageElementRequest> {

  @Inject ObjectStorageService storageService;

  @Inject ContentElementRepository contentElementRepository;

  @Override
  public ContentElement creteContentElementFromRequest(ImageElementRequest request, File file) {

    if (file == null) {
      throw new NoFileProvidedException("Image file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid image");
    }

    try {
      ImageElement contentElement = new ImageElement();
      contentElement.title = request.title;
      contentElement.type = ContentType.Image;
      contentElement.s3Key = storageService.uploadObject(contentElement, file);
      contentElement.originalFileName = request.originalFileName;

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

  private boolean isFileTypeValid(File file) {
    try {
      String contentType = Files.probeContentType(file.toPath());
      if (contentType == null) {
        return false;
      }
      return contentType.startsWith("image/");
    } catch (IOException e) {
      return false;
    }
  }
}
