/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.content;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.request.content.ContentElementRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContentElementProcessorManagerTest {

  @Inject ContentElementProcessorManager manager;

  // Dummy Request and Element for testing
  static class MockRequest extends ContentElementRequest {}

  static class MockElement extends ContentElement {}

  static class UnknownRequest extends ContentElementRequest {}

  /**
   * This inner class is a real CDI bean that Quarkus will discover and inject into the manager's
   * Instance stream during the test.
   */
  @ApplicationScoped
  static class MockProcessor implements ContentElementRequestProcessor<MockRequest> {
    @Override
    public ContentElement creteContentElementFromRequest(MockRequest request, FileUpload file) {
      return new MockElement();
    }

    @Override
    public boolean supports(ContentElementRequest request) {
      return request instanceof MockRequest;
    }
  }

  @Test
  void testCreateContentElementFromRequest_Success() {
    // Arrange
    MockRequest request = new MockRequest();
    FileUpload file = mock(FileUpload.class);

    // Act
    ContentElement result = manager.createContentElementFromRequest(request, file);

    // Assert
    assertNotNull(result);
    assertTrue(result instanceof MockElement);
  }

  @Test
  void testCreateContentElementFromRequest_NoProcessorFound() {
    // Arrange
    UnknownRequest request = new UnknownRequest();
    FileUpload file = mock(FileUpload.class);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              manager.createContentElementFromRequest(request, file);
            });

    assertTrue(exception.getMessage().contains("No processor for type"));
  }
}
