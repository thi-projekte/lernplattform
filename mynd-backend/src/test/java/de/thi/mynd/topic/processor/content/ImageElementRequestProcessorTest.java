/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
import de.thi.mynd.topic.entity.ImageElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.ImageElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ImageElementRequestProcessorTest {

  @Inject ImageElementRequestProcessor processor;

  @InjectMock ObjectStorageService storageService;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    ImageElementRequest request = new ImageElementRequest();
    request.title = "Hero Image";
    request.originalFileName = "banner.png";

    FileUpload mockFile = mock(FileUpload.class);
    Path mockPath = mock(Path.class);
    File tempFile = new File("src/test/resources/banner.png");

    when(mockFile.contentType()).thenReturn("image/png");
    when(mockFile.uploadedFile()).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(tempFile);

    String expectedS3Key = "images/uuid/banner.png";
    when(storageService.uploadObject(any(ImageElement.class), eq(tempFile), eq("banner.png")))
        .thenReturn(expectedS3Key);

    // Act
    var result = processor.creteContentElementFromRequest(request, mockFile);

    // Assert
    assertTrue(result instanceof ImageElement);
    ImageElement imageResult = (ImageElement) result;

    assertEquals("Hero Image", imageResult.title);
    assertEquals(ContentType.IMAGE, imageResult.type);
    assertEquals(expectedS3Key, imageResult.s3Key);

    verify(contentElementRepository).persist(any(ImageElement.class));
    verify(contentElementRepository).persistAndFlush(any(ImageElement.class));
  }

  @Test
  void testCreteContentElementFromRequest_NoFileThrowsException() {
    ImageElementRequest request = new ImageElementRequest();

    assertThrows(
        NoFileProvidedException.class,
        () -> processor.creteContentElementFromRequest(request, null));
  }

  @Test
  void testCreteContentElementFromRequest_InvalidTypeThrowsException() {
    // Arrange
    ImageElementRequest request = new ImageElementRequest();
    FileUpload mockFile = mock(FileUpload.class);
    when(mockFile.contentType()).thenReturn("application/pdf");

    // Act & Assert
    assertThrows(
        InvalidFileTypeException.class,
        () -> processor.creteContentElementFromRequest(request, mockFile));
  }

  @Test
  void testSupports() {
    assertTrue(processor.supports(new ImageElementRequest()));
    // verify it returns false for other types (if you have them)
  }
}
