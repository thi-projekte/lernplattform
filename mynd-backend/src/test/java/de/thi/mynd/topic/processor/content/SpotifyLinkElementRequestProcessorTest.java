package de.thi.mynd.topic.processor.content;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.SpotifyLinkElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.SpotifyLinkElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SpotifyLinkElementRequestProcessorTest {

  @Inject SpotifyLinkElementRequestProcessor processor;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    SpotifyLinkElementRequest request = new SpotifyLinkElementRequest();
    request.title = "Favorite Playlist";
    request.uri = "spotify:playlist:37i9dQZF1DXcBWIGvYBM31";

    // File is not used by this processor
    FileUpload unusedFile = null;

    // Act
    var result = processor.creteContentElementFromRequest(request, unusedFile);

    // Assert
    assertTrue(result instanceof SpotifyLinkElement);
    SpotifyLinkElement spotifyResult = (SpotifyLinkElement) result;

    assertEquals("Favorite Playlist", spotifyResult.title);
    assertEquals(ContentType.SPOTIFY_LINK, spotifyResult.type);
    assertEquals(request.uri, spotifyResult.uri);

    // Verify the entity was persisted to the database
    verify(contentElementRepository, times(1)).persistAndFlush(any(SpotifyLinkElement.class));
  }

  @Test
  void testSupports() {
    assertTrue(processor.supports(new SpotifyLinkElementRequest()));

    // Verify it returns false for a different request type
    assertFalse(
        processor.supports(mock(de.thi.mynd.topic.requests.content.ImageElementRequest.class)));
  }
}
