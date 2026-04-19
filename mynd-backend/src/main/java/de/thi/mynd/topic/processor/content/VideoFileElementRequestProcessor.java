package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.VideoFileElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;

@ApplicationScoped
public final class VideoFileElementRequestProcessor
    implements ContentElementRequestProcessor<VideoFileElementRequest> {

  @Inject ObjectStorageService storageService;

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(VideoFileElementRequest request, FileUpload file) {

    if (file == null) {
      throw new NoFileProvidedException("Video file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid video file");
    }

    try {
      VideoFileElement contentElement = new VideoFileElement();
      contentElement.title = request.title;
      contentElement.type = ContentType.VideoFile;
      contentElement.s3Key = storageService.uploadObject(contentElement, file.uploadedFile().toFile());
      contentElement.originalFileName = request.originalFileName;

      contentElementRepository.persistAndFlush(contentElement);

      return contentElement;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof VideoFileElementRequest;
  }

  private boolean isFileTypeValid(FileUpload file) {
      String contentType = file.contentType();
      if (contentType == null) {
        return false;
      }
      return contentType.startsWith("video/");
  }
}
