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
import de.thi.mynd.topic.entity.RtfElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.RtfElementRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RtfElementRequestProcessorTest {

  @Inject RtfElementRequestProcessor processor;

  @InjectMock ContentElementRepository contentElementRepository;

  @Test
  void testCreteContentElementFromRequest_Success() {
    // Arrange
    RtfElementRequest request = new RtfElementRequest();
    request.title = "Rich Text Note";
    request.rtfText = "{\\rtf1\\ansi This is some sample text}";

    // The file is not used in this specific implementation, so we pass null or a mock
    FileUpload unusedFile = null;

    // Act
    var result = processor.creteContentElementFromRequest(request, unusedFile);

    // Assert
    assertTrue(result instanceof RtfElement);
    RtfElement rtfResult = (RtfElement) result;

    assertEquals("Rich Text Note", rtfResult.title);
    assertEquals(ContentType.RTF, rtfResult.type);
    assertEquals(request.rtfText, rtfResult.rtfText);

    // Verify only persistAndFlush is called (Shell persist is not used here)
    verify(contentElementRepository, times(1)).persistAndFlush(any(RtfElement.class));
  }

  @Test
  void testSupports() {
    assertTrue(processor.supports(new RtfElementRequest()));
    assertFalse(
        processor.supports(mock(de.thi.mynd.topic.request.content.PdfElementRequest.class)));
  }
}
