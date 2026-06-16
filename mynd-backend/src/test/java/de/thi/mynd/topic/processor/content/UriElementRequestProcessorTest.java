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
import de.thi.mynd.topic.entity.UriElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.UriElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UriElementRequestProcessorTest {

  @Inject UriElementRequestProcessor processor;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    UriElementRequest request = new UriElementRequest();
    request.title = "External Resource";
    request.uri = "https://www.thi.de";

    // File is not used by this processor
    FileUpload unusedFile = null;

    // Act
    var result = processor.creteContentElementFromRequest(request, unusedFile);

    // Assert
    assertTrue(result instanceof UriElement);
    UriElement uriResult = (UriElement) result;

    assertEquals("External Resource", uriResult.title);
    assertEquals(ContentType.URI, uriResult.type);
    assertEquals("https://www.thi.de", uriResult.uri);

    // Verify the database persistence
    verify(contentElementRepository, times(1)).persistAndFlush(any(UriElement.class));
  }

  @Test
  void testSupports() {
    // Arrange
    UriElementRequest validRequest = new UriElementRequest();

    // Act & Assert
    assertTrue(processor.supports(validRequest));
    assertFalse(processor.supports(null));
  }
}
