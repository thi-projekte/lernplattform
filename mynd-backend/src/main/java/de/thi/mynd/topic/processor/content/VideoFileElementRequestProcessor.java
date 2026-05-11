package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.exception.FileTooLargeException;
import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.VideoFileElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class VideoFileElementRequestProcessor
    implements ContentElementRequestProcessor<VideoFileElementRequest> {

  private static final long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024;

  @Inject ObjectStorageService storageService;

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(
      VideoFileElementRequest request, FileUpload file) {

    if (file == null) {
      throw new NoFileProvidedException("Video file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid video file");
    }

    if (file.size() > MAX_FILE_SIZE_BYTES) {
      throw new FileTooLargeException("Video file must not exceed 100 MB");
    }

    VideoFileElement contentElement = new VideoFileElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.VIDEO_FILE;
    contentElement.icon = request.icon;
    contentElement.s3Key = "";
    contentElement.originalFileName = request.originalFileName;

    contentElementRepository.persist(contentElement);

    contentElement.s3Key =
        storageService.uploadObject(
            contentElement, file.uploadedFile().toFile(), request.originalFileName);

    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created Videofile element with id: %s", contentElement.id);

    return contentElement;
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
