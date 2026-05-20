package de.thi.mynd.progressTracking.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.exception.ContentElementLearnProgressAlreadyCompletedException;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressAlreadyStartedException;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressNotStartedException;
import de.thi.mynd.progressTracking.service.LearnProgressService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LearnProgressResourceTest {

  @InjectMock LearnProgressService learnProgressService;

  // ---------------------------------------------------------------------------
  // GET /learn-progress/topics/{topicId}
  // ---------------------------------------------------------------------------

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void getPersonalProgressForTopic_returns200_whenTopicStarted() {
    UUID topicId = UUID.randomUUID();
    TopicLearnProgressDto dto = TopicLearnProgressDto.builder().build();

    when(learnProgressService.getLearnProgressForTopic(topicId)).thenReturn(dto);

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/learn-progress/topics/{topicId}", topicId)
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void getPersonalProgressForTopic_returns404_whenTopicNotStarted() {
    UUID topicId = UUID.randomUUID();

    when(learnProgressService.getLearnProgressForTopic(topicId))
        .thenThrow(
            new TopicLearnProgressNotStartedException("This topic has not been started yet"));

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/learn-progress/topics/{topicId}", topicId)
        .then()
        .statusCode(409);
  }

  @Test
  void getPersonalProgressForTopic_returns401_whenUnauthenticated() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/learn-progress/topics/{topicId}", UUID.randomUUID())
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void getPersonalProgressForTopic_passesCorrectTopicId() {
    UUID topicId = UUID.randomUUID();

    when(learnProgressService.getLearnProgressForTopic(topicId))
        .thenReturn(TopicLearnProgressDto.builder().build());

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/learn-progress/topics/{topicId}", topicId)
        .then()
        .statusCode(200);

    verify(learnProgressService).getLearnProgressForTopic(topicId);
  }

  // ---------------------------------------------------------------------------
  // POST /learn-progress/topics/{topicId}/start
  // ---------------------------------------------------------------------------

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void startLearningTopic_returns200_whenNotYetStarted() {
    UUID topicId = UUID.randomUUID();
    doNothing().when(learnProgressService).startLearningTopicAsCurrentUser(topicId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/start", topicId)
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void startLearningTopic_returns409_whenAlreadyStarted() {
    UUID topicId = UUID.randomUUID();

    doThrow(new TopicLearnProgressAlreadyStartedException("This topic has already been started"))
        .when(learnProgressService)
        .startLearningTopicAsCurrentUser(topicId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/start", topicId)
        .then()
        .statusCode(409);
  }

  @Test
  void startLearningTopic_returns401_whenUnauthenticated() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/start", UUID.randomUUID())
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void startLearningTopic_passesCorrectTopicId() {
    UUID topicId = UUID.randomUUID();
    doNothing().when(learnProgressService).startLearningTopicAsCurrentUser(topicId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/start", topicId)
        .then()
        .statusCode(200);

    verify(learnProgressService).startLearningTopicAsCurrentUser(topicId);
  }

  // ---------------------------------------------------------------------------
  // POST /learn-progress/topics/{topicId}/complete
  // ---------------------------------------------------------------------------

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void manuallyCompleteTopic_returns200_whenTopicStarted() {
    UUID topicId = UUID.randomUUID();
    doNothing().when(learnProgressService).manuallyCompleteTopicAsCurrentUser(topicId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/complete", topicId)
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void manuallyCompleteTopic_returns404_whenTopicNotStarted() {
    UUID topicId = UUID.randomUUID();

    doThrow(new TopicLearnProgressNotStartedException("This topic has not been started yet"))
        .when(learnProgressService)
        .manuallyCompleteTopicAsCurrentUser(topicId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/complete", topicId)
        .then()
        .statusCode(409);
  }

  @Test
  void manuallyCompleteTopic_returns401_whenUnauthenticated() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/complete", UUID.randomUUID())
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void manuallyCompleteTopic_passesCorrectTopicId() {
    UUID topicId = UUID.randomUUID();
    doNothing().when(learnProgressService).manuallyCompleteTopicAsCurrentUser(topicId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/topics/{topicId}/complete", topicId)
        .then()
        .statusCode(200);

    verify(learnProgressService).manuallyCompleteTopicAsCurrentUser(topicId);
  }

  // ---------------------------------------------------------------------------
  // POST /learn-progress/content-elements/{contentElementId}/complete
  // ---------------------------------------------------------------------------

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void completeContentElement_returns200_whenValid() {
    UUID contentElementId = UUID.randomUUID();
    doNothing()
        .when(learnProgressService)
        .completeLearningContentElementAsCurrentUser(contentElementId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/complete", contentElementId)
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void completeContentElement_returns409_whenAlreadyCompleted() {
    UUID contentElementId = UUID.randomUUID();

    doThrow(
            new ContentElementLearnProgressAlreadyCompletedException(
                "Content element already completed"))
        .when(learnProgressService)
        .completeLearningContentElementAsCurrentUser(contentElementId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/complete", contentElementId)
        .then()
        .statusCode(409);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void completeContentElement_returns404_whenTopicNotStarted() {
    UUID contentElementId = UUID.randomUUID();

    doThrow(new TopicLearnProgressNotStartedException("This topic has not been started yet"))
        .when(learnProgressService)
        .completeLearningContentElementAsCurrentUser(contentElementId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/complete", contentElementId)
        .then()
        .statusCode(409);
  }

  @Test
  void completeContentElement_returns401_whenUnauthenticated() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/complete", UUID.randomUUID())
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void completeContentElement_passesCorrectContentElementId() {
    UUID contentElementId = UUID.randomUUID();
    doNothing()
        .when(learnProgressService)
        .completeLearningContentElementAsCurrentUser(contentElementId);

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/complete", contentElementId)
        .then()
        .statusCode(200);

    verify(learnProgressService).completeLearningContentElementAsCurrentUser(contentElementId);
  }

  @Test
  @TestSecurity(user = "testuser", roles = "user,authorizedUser")
  void completeContentElement_serviceCalledExactlyOnce() {
    UUID contentElementId = UUID.randomUUID();
    doNothing().when(learnProgressService).completeLearningContentElementAsCurrentUser(any());

    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/complete", contentElementId)
        .then()
        .statusCode(200);

    verify(learnProgressService, times(1))
        .completeLearningContentElementAsCurrentUser(contentElementId);
  }

  @Test
  @TestSecurity(user = "user-123", roles = "user,authorizedUser")
  void resetTopicLearningProgress_shouldReturn200_whenProgressExists() {
    UUID topicId = UUID.randomUUID();
    doNothing().when(learnProgressService).resetTopicLearningProgress(topicId);

    given()
        .pathParam("topicId", topicId)
        .when()
        .post("/learn-progress/topics/{topicId}/reset")
        .then()
        .statusCode(200);

    verify(learnProgressService).resetTopicLearningProgress(topicId);
  }

  @Test
  @TestSecurity(user = "user-123", roles = "user,authorizedUser")
  void resetTopicLearningProgress_shouldReturn409_whenProgressNotStarted() {
    UUID topicId = UUID.randomUUID();
    doThrow(new TopicLearnProgressNotStartedException("This topic has not been started yet"))
        .when(learnProgressService)
        .resetTopicLearningProgress(topicId);

    given()
        .pathParam("topicId", topicId)
        .when()
        .post("/learn-progress/topics/{topicId}/reset")
        .then()
        .statusCode(409);
  }

  @Test
  void resetTopicLearningProgress_shouldReturn401_whenUnauthenticated() {
    UUID topicId = UUID.randomUUID();

    given()
        .pathParam("topicId", topicId)
        .when()
        .post("/learn-progress/topics/{topicId}/reset")
        .then()
        .statusCode(401);

    verifyNoInteractions(learnProgressService);
  }

  // ── resetContentElementLearningProgress ────────────────────────────────

  @Test
  @TestSecurity(user = "user-123", roles = "user,authorizedUser")
  void resetContentElementLearningProgress_shouldReturn200_whenProgressExists() {
    UUID contentElementId = UUID.randomUUID();
    doNothing().when(learnProgressService).resetContentElementLearningProgress(contentElementId);

    given()
        .pathParam("contentElementId", contentElementId)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/reset")
        .then()
        .statusCode(200);

    verify(learnProgressService).resetContentElementLearningProgress(contentElementId);
  }

  @Test
  @TestSecurity(user = "user-123", roles = "user,authorizedUser")
  void resetContentElementLearningProgress_shouldReturn409_whenProgressNotStarted() {
    UUID contentElementId = UUID.randomUUID();
    doThrow(
            new TopicLearnProgressNotStartedException(
                "This content element has not been started yet"))
        .when(learnProgressService)
        .resetContentElementLearningProgress(contentElementId);

    given()
        .pathParam("contentElementId", contentElementId)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/reset")
        .then()
        .statusCode(409);
  }

  @Test
  void resetContentElementLearningProgress_shouldReturn401_whenUnauthenticated() {
    UUID contentElementId = UUID.randomUUID();

    given()
        .pathParam("contentElementId", contentElementId)
        .when()
        .post("/learn-progress/content-elements/{contentElementId}/reset")
        .then()
        .statusCode(401);

    verifyNoInteractions(learnProgressService);
  }
}
