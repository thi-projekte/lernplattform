/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.processor.content;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.PdfElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.PdfElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PdfElementRequestProcessorTest {

  @Inject PdfElementRequestProcessor processor;

  @InjectMock ObjectStorageService storageService;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    PdfElementRequest request = new PdfElementRequest();
    request.title = "User Manual";
    request.originalFileName = "guide.pdf";

    FileUpload mockFile = mock(FileUpload.class);
    Path mockPath = mock(Path.class);
    File tempFile = new File("src/test/resources/guide.pdf");

    when(mockFile.contentType()).thenReturn("application/pdf");
    when(mockFile.uploadedFile()).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(tempFile);

    String expectedS3Key = "pdf/uuid/guide.pdf";
    when(storageService.uploadObject(any(PdfElement.class), eq(tempFile), eq("guide.pdf")))
        .thenReturn(expectedS3Key);

    // Act
    var result = processor.creteContentElementFromRequest(request, mockFile);

    // Assert
    assertTrue(result instanceof PdfElement);
    PdfElement pdfResult = (PdfElement) result;

    assertEquals("User Manual", pdfResult.title);
    assertEquals(ContentType.PDF, pdfResult.type);
    assertEquals(expectedS3Key, pdfResult.s3Key);

    verify(contentElementRepository).persist(any(PdfElement.class));
    verify(contentElementRepository).persistAndFlush(any(PdfElement.class));
  }

  @Test
  void testCreteContentElementFromRequest_NoFileThrowsException() {
    PdfElementRequest request = new PdfElementRequest();

    NoFileProvidedException ex =
        assertThrows(
            NoFileProvidedException.class,
            () -> processor.creteContentElementFromRequest(request, null));
    assertEquals("Pdf file is missing", ex.getMessage());
  }

  @Test
  void testCreteContentElementFromRequest_InvalidTypeThrowsException() {
    // Arrange
    PdfElementRequest request = new PdfElementRequest();
    FileUpload mockFile = mock(FileUpload.class);
    // Note: Even application/x-pdf would fail here because your code uses .equals()
    when(mockFile.contentType()).thenReturn("text/plain");

    // Act & Assert
    InvalidFileTypeException ex =
        assertThrows(
            InvalidFileTypeException.class,
            () -> processor.creteContentElementFromRequest(request, mockFile));
    assertEquals("The file is not a valid pdf", ex.getMessage());
  }

  @Test
  void testSupports() {
    assertTrue(processor.supports(new PdfElementRequest()));
    assertFalse(processor.supports(null));
  }
}
