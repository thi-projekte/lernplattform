/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.dto.content.PdfElementDto;
import de.thi.mynd.topic.request.content.PdfElementRequest;
import de.thi.mynd.topic.service.ContentElementService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContentElementResourceTest {

  @InjectMock ContentElementService contentElementService;

  @Test
  @TestSecurity(
      user = "test-builder",
      roles = {"builder", "authorizedUser"})
  void testCreateContentElement() {
    // Arrange
    String jsonBody =
        "{"
            + "\"title\": \"Test PDF\","
            + "\"originalFileName\": \"test.pdf\","
            + "\"type\": \"PDF\","
            + "\"icon\": \"spotify\""
            + "}";

    ContentElementDto responseDto =
        PdfElementDto.builder().id(UUID.randomUUID()).title("Test PDF").build();

    when(contentElementService.createContentElement(
            any(PdfElementRequest.class), any(FileUpload.class)))
        .thenReturn(responseDto);

    // Act & Assert
    given()
        .multiPart("request", jsonBody, MediaType.APPLICATION_JSON)
        .multiPart("file", "test.pdf", "This is the file content".getBytes())
        .when()
        .post("/content-elements")
        .then()
        .statusCode(200)
        .body("title", is("Test PDF"));

    verify(contentElementService).createContentElement(any(), any());
  }

  @Test
  @TestSecurity(
      user = "test-builder",
      roles = {"builder", "authorizedUser"})
  void testDeleteContentElement() {
    // Arrange
    UUID elementId = UUID.randomUUID();

    // Act & Assert
    given()
        .pathParam("elementId", elementId)
        .when()
        .delete("/content-elements/{elementId}")
        .then()
        .statusCode(200);

    verify(contentElementService).deleteContentElement(elementId);
  }

  @Test
  @TestSecurity(user = "unauthorized-user", roles = "authorizedUser")
  void testDeleteContentElement_Forbidden() {
    // Arrange
    UUID elementId = UUID.randomUUID();

    // Act & Assert (Should return 403 because role is 'user' not 'builder')
    given()
        .pathParam("elementId", elementId)
        .when()
        .delete("/content-elements/{elementId}")
        .then()
        .statusCode(403);
  }
}
