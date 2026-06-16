/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.request.IndexCardRequest;
import de.thi.mynd.topic.service.IndexCardService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IndexCardResourceTest {

  @InjectMock IndexCardService indexCardService;

  // ==========================================
  // SECURITY / AUTHORIZATION TESTS
  // ==========================================

  @Test
  void testEndpoint_Returns401_WhenAnonymous() {
    // No @TestSecurity annotation means the request is completely anonymous
    given().contentType(ContentType.JSON).when().post("/index-cards").then().statusCode(401);
  }

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"authorizedUser"})
  void testCreateEndpoint_Returns403_WhenMissingBuilderRole() {
    IndexCardRequest request = new IndexCardRequest();
    request.question = "Test?";
    request.answer = "Ans";

    // Has 'authorizedUser' (class level rule), but lacks 'builder' (method level rule)
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/index-cards")
        .then()
        .statusCode(403);
  }

  // ==========================================
  // POST /index-cards (Create)
  // ==========================================

  @Test
  @TestSecurity(
      user = "bob",
      roles = {"authorizedUser", "builder"})
  void testCreateContentElement_Success() {
    IndexCardRequest request = new IndexCardRequest();
    request.question = "What is Quarkus?";
    request.answer = "Supersonic Java";

    IndexCardDto mockDto = IndexCardDto.builder().build();
    // Set properties on mockDto if necessary to verify JSON path matching

    when(indexCardService.createIndexCard(any(IndexCardRequest.class))).thenReturn(mockDto);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/index-cards")
        .then()
        .statusCode(200); // Note: Since your resource returns the DTO directly instead of
    // Response.created(), JAX-RS defaults to a 200 OK

    verify(indexCardService).createIndexCard(any(IndexCardRequest.class));
  }

  // ==========================================
  // DELETE /index-cards/{elementId}
  // ==========================================

  @Test
  @TestSecurity(
      user = "bob",
      roles = {"authorizedUser", "builder"})
  void testDeleteContentElement_Success() {
    UUID cardId = UUID.randomUUID();

    doNothing().when(indexCardService).deleteIndexCard(cardId);

    given()
        .pathParam("elementId", cardId.toString())
        .when()
        .delete("/index-cards/{elementId}")
        .then()
        .statusCode(200);

    verify(indexCardService).deleteIndexCard(cardId);
  }

  @Test
  @TestSecurity(
      user = "bob",
      roles = {"authorizedUser", "builder"})
  void testDeleteContentElement_Returns404_WhenServiceThrowsNotFound() {
    UUID cardId = UUID.randomUUID();

    // Simulating the exception your service layer throws
    doThrow(new EntityInstanceNotFoundException("Not Found"))
        .when(indexCardService)
        .deleteIndexCard(cardId);

    given()
        .pathParam("elementId", cardId.toString())
        .when()
        .delete("/index-cards/{elementId}")
        .then()
        .statusCode(404);
    // Note: This relies on you having an ExceptionMapper that maps EntityInstanceNotFoundException
    // -> 404 Status.
  }
}
