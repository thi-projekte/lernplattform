package de.thi.mynd.topic.processor.content;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.VideoFileElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.VideoFileElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class VideoFileElementRequestProcessorTest {

  @Inject VideoFileElementRequestProcessor processor;

  @InjectMock ObjectStorageService storageService;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    VideoFileElementRequest request = new VideoFileElementRequest();
    request.title = "Instructional Video";
    request.originalFileName = "tutorial.mp4";

    FileUpload mockFile = mock(FileUpload.class);
    Path mockPath = mock(Path.class);
    File tempFile = new File("src/test/resources/tutorial.mp4");

    when(mockFile.contentType()).thenReturn("video/mp4");
    when(mockFile.uploadedFile()).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(tempFile);

    String expectedS3Key = "video/uuid/tutorial.mp4";
    when(storageService.uploadObject(any(VideoFileElement.class), eq(tempFile), eq("tutorial.mp4")))
        .thenReturn(expectedS3Key);

    // Act
    var result = processor.creteContentElementFromRequest(request, mockFile);

    // Assert
    assertTrue(result instanceof VideoFileElement);
    VideoFileElement videoResult = (VideoFileElement) result;

    assertEquals("Instructional Video", videoResult.title);
    assertEquals(ContentType.VIDEO_FILE, videoResult.type);
    assertEquals(expectedS3Key, videoResult.s3Key);

    verify(contentElementRepository).persist(any(VideoFileElement.class));
    verify(contentElementRepository).persistAndFlush(any(VideoFileElement.class));
  }

  @Test
  void testCreteContentElementFromRequest_NoFileThrowsException() {
    VideoFileElementRequest request = new VideoFileElementRequest();

    NoFileProvidedException ex =
        assertThrows(
            NoFileProvidedException.class,
            () -> processor.creteContentElementFromRequest(request, null));
    assertEquals("Video file is missing", ex.getMessage());
  }

  @Test
  void testCreteContentElementFromRequest_InvalidTypeThrowsException() {
    // Arrange
    VideoFileElementRequest request = new VideoFileElementRequest();
    FileUpload mockFile = mock(FileUpload.class);
    when(mockFile.contentType()).thenReturn("application/octet-stream");

    // Act & Assert
    InvalidFileTypeException ex =
        assertThrows(
            InvalidFileTypeException.class,
            () -> processor.creteContentElementFromRequest(request, mockFile));
    assertEquals("The file is not a valid video file", ex.getMessage());
  }

  @Test
  void testSupports() {
    assertTrue(processor.supports(new VideoFileElementRequest()));
    assertFalse(
        processor.supports(mock(de.thi.mynd.topic.request.content.UriElementRequest.class)));
  }
}
