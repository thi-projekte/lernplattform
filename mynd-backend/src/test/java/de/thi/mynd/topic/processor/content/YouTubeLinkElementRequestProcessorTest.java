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
import static org.mockito.Mockito.*;

import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.YouTubeLinkElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.YouTubeLinkElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class YouTubeLinkElementRequestProcessorTest {

  @Inject YouTubeLinkElementRequestProcessor processor;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    YouTubeLinkElementRequest request = new YouTubeLinkElementRequest();
    request.title = "Educational Video";
    request.uri = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    // File is not used by this processor
    FileUpload unusedFile = null;

    // Act
    var result = processor.creteContentElementFromRequest(request, unusedFile);

    // Assert
    assertTrue(result instanceof YouTubeLinkElement);
    YouTubeLinkElement youtubeResult = (YouTubeLinkElement) result;

    assertEquals("Educational Video", youtubeResult.title);
    assertEquals(ContentType.YOUTUBE_LINK, youtubeResult.type);
    assertEquals(request.uri, youtubeResult.uri);

    // Verify repository interaction
    verify(contentElementRepository, times(1)).persistAndFlush(any(YouTubeLinkElement.class));
  }

  @Test
  void testSupports() {
    // Assert
    assertTrue(processor.supports(new YouTubeLinkElementRequest()));
    assertFalse(processor.supports(null));
  }
}
