package de.thi.mynd.topic.processor.content;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.AudioFileElement;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.AudioFileElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AudioFileElementRequestProcessorTest {

  @Inject AudioFileElementRequestProcessor processor;

  @InjectMock ObjectStorageService storageService;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    AudioFileElementRequest request = new AudioFileElementRequest();
    request.title = "Meditation Audio";
    request.originalFileName = "om.mp3";

    FileUpload mockFile = mock(FileUpload.class);
    Path mockPath = mock(Path.class);
    File tempFile = new File("src/test/resources/test-audio.mp3"); // Doesn't need to exist for mock

    when(mockFile.contentType()).thenReturn("audio/mpeg");
    when(mockFile.uploadedFile()).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(tempFile);

    String expectedS3Key = "audio/path/om.mp3";
    when(storageService.uploadObject(any(AudioFileElement.class), eq(tempFile), eq("om.mp3")))
        .thenReturn(expectedS3Key);

    // Act
    ContentElement result = processor.creteContentElementFromRequest(request, mockFile);

    // Assert
    assertTrue(result instanceof AudioFileElement);
    AudioFileElement audioResult = (AudioFileElement) result;

    assertEquals("Meditation Audio", audioResult.title);
    assertEquals(ContentType.AUDIO_FILE, audioResult.type);
    assertEquals(expectedS3Key, audioResult.s3Key);

    verify(contentElementRepository, times(1)).persist(any(AudioFileElement.class));
    verify(contentElementRepository, times(1)).persistAndFlush(any(AudioFileElement.class));
  }

  @Test
  void testCreteContentElementFromRequest_NoFileThrowsException() {
    // Arrange
    AudioFileElementRequest request = new AudioFileElementRequest();

    // Act & Assert
    NoFileProvidedException ex =
        assertThrows(
            NoFileProvidedException.class,
            () -> {
              processor.creteContentElementFromRequest(request, null);
            });
    assertEquals("Image file is missing", ex.getMessage());
  }

  @Test
  void testCreteContentElementFromRequest_InvalidTypeThrowsException() {
    // Arrange
    AudioFileElementRequest request = new AudioFileElementRequest();
    FileUpload mockFile = mock(FileUpload.class);
    when(mockFile.contentType()).thenReturn("image/png"); // Wrong type

    // Act & Assert
    InvalidFileTypeException ex =
        assertThrows(
            InvalidFileTypeException.class,
            () -> {
              processor.creteContentElementFromRequest(request, mockFile);
            });
    assertEquals("The file is not a valid image", ex.getMessage());
  }

  @Test
  void testSupports() {
    assertTrue(processor.supports(new AudioFileElementRequest()));
    assertFalse(
        processor.supports(mock(de.thi.mynd.topic.requests.content.ContentElementRequest.class)));
  }
}
