/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.exception.FileTooLargeException;
import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.AudioFileElement;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.AudioFileElementRequest;
import de.thi.mynd.topic.request.content.ContentElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class AudioFileElementRequestProcessor
    implements ContentElementRequestProcessor<AudioFileElementRequest> {
  private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;
  @Inject ObjectStorageService storageService;

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(
      AudioFileElementRequest request, FileUpload file) {

    if (file == null) {
      throw new NoFileProvidedException("Audio file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid audio");
    }
    if (file.size() > MAX_FILE_SIZE_BYTES) {
      throw new FileTooLargeException("Audio file must not exceed 20 MB");
    }
    AudioFileElement contentElement = new AudioFileElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.AUDIO_FILE;
    contentElement.icon = request.icon;
    contentElement.s3Key = "";
    contentElement.originalFileName = request.originalFileName;

    contentElementRepository.persist(contentElement);

    contentElement.s3Key =
        storageService.uploadObject(
            contentElement, file.uploadedFile().toFile(), request.originalFileName);

    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created audio content element with id: %s", contentElement.id);

    return contentElement;
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof AudioFileElementRequest;
  }

  private boolean isFileTypeValid(FileUpload file) {
    String contentType = file.contentType();
    ;
    if (contentType == null) {
      return false;
    }
    return contentType.startsWith("audio/");
  }
}
