/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.request.TopicNoteRequest;
import de.thi.mynd.progressTracking.service.TopicNoteService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicNoteResourceTest {

  @InjectMock TopicNoteService topicNoteService;

  private final UUID MOCK_TOPIC_ID = UUID.randomUUID();

  // ==========================================
  // Security & Authorization Tests
  // ==========================================

  @Test
  void whenUnauthorized_shouldReturn401() {
    // No @TestSecurity annotation means the request is anonymous
    given()
        .pathParam("topicId", MOCK_TOPIC_ID)
        .when()
        .get("/topic-notes/{topicId}")
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "wrongUser", roles = "wrongRole")
  void whenForbiddenRole_shouldReturn403() {
    given()
        .pathParam("topicId", MOCK_TOPIC_ID)
        .when()
        .get("/topic-notes/{topicId}")
        .then()
        .statusCode(403);
  }

  // ==========================================
  // GET Endpoint Tests
  // ==========================================

  @Test
  @TestSecurity(user = "testUser", roles = "authorizedUser")
  void getTopicNote_whenAuthorized_returns200AndDto() {
    // Arrange
    TopicNoteDto expectedDto = TopicNoteDto.builder().build();
    // Assuming your TopicNoteDto has a content field or similar string representation
    // For demonstration, let's say it maps cleanly to JSON

    when(topicNoteService.getTopicNoteForCurrentUser(MOCK_TOPIC_ID)).thenReturn(expectedDto);

    // Act & Assert
    given()
        .pathParam("topicId", MOCK_TOPIC_ID)
        .when()
        .get("/topic-notes/{topicId}")
        .then()
        .statusCode(200);
  }

  // ==========================================
  // PUT Endpoint Tests
  // ==========================================

  @Test
  @TestSecurity(user = "testUser", roles = "authorizedUser")
  void updateTopicNote_withValidRequest_returns200AndUpdatedDto() {
    // Arrange
    TopicNoteRequest request = new TopicNoteRequest();
    request.content = "Updated note content";

    TopicNoteDto updatedDto = TopicNoteDto.builder().build();

    when(topicNoteService.updateTopicNoteForCurrentUser(
            eq(MOCK_TOPIC_ID), any(TopicNoteRequest.class)))
        .thenReturn(updatedDto);

    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .pathParam("topicId", MOCK_TOPIC_ID)
        .when()
        .put("/topic-notes/{topicId}")
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "testUser", roles = "authorizedUser")
  void updateTopicNote_withInvalidRequest_returns400() {
    // Arrange
    TopicNoteRequest invalidRequest = new TopicNoteRequest();
    // Assuming your @Valid triggers on a specific condition (e.g., content is null/empty if
    // @NotBlank is used)
    invalidRequest.content = null;

    // Act & Assert
    // Note: If you have strict @NotNull/@NotBlank validation rules, Quarkus will block the request
    // with a 400 Bad Request before hitting the service layer.
    given()
        .contentType(ContentType.JSON)
        .body(invalidRequest)
        .pathParam("topicId", MOCK_TOPIC_ID)
        .when()
        .put("/topic-notes/{topicId}")
        .then()
        .statusCode(400);
  }
}
