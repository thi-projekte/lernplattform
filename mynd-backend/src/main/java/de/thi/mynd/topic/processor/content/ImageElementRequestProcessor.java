/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.exception.FileTooLargeException;
import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.ImageElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.ContentElementRequest;
import de.thi.mynd.topic.request.content.ImageElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class ImageElementRequestProcessor
    implements ContentElementRequestProcessor<ImageElementRequest> {

  private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
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

    if (file.size() > MAX_FILE_SIZE_BYTES) {
      throw new FileTooLargeException("Image file must not exceed 10 MB");
    }

    ImageElement contentElement = new ImageElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.IMAGE;
    contentElement.icon = request.icon;
    contentElement.s3Key = "";
    contentElement.originalFileName = request.originalFileName;

    contentElementRepository.persist(contentElement);

    contentElement.s3Key =
        storageService.uploadObject(
            contentElement, file.uploadedFile().toFile(), request.originalFileName);

    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created image content element with id: %s", contentElement.id);

    return contentElement;
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
