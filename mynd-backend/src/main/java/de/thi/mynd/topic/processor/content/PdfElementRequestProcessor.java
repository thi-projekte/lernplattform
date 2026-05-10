package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.PdfElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import de.thi.mynd.topic.requests.content.PdfElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class PdfElementRequestProcessor
    implements ContentElementRequestProcessor<PdfElementRequest> {

  @Inject ObjectStorageService storageService;

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(PdfElementRequest request, FileUpload file) {

    if (file == null) {
      throw new NoFileProvidedException("Pdf file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid pdf");
    }

    PdfElement contentElement = new PdfElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.PDF;
    contentElement.icon = request.icon;
    contentElement.s3Key = "";
    contentElement.originalFileName = request.originalFileName;

    contentElementRepository.persist(contentElement);

    contentElement.s3Key =
        storageService.uploadObject(
            contentElement, file.uploadedFile().toFile(), request.originalFileName);

    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created PDF content element with id: %s", contentElement.id);

    return contentElement;
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof PdfElementRequest;
  }

  private boolean isFileTypeValid(FileUpload file) {
    String contentType = file.contentType();
    if (contentType == null) {
      return false;
    }
    return contentType.equals("application/pdf");
  }
}
