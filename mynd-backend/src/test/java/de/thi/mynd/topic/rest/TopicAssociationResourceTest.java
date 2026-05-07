package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.*;

import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.service.TopicAssociationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TopicAssociationResourceTest {

  @InjectMock TopicAssociationService associationService;

  @Test
  @TestSecurity(
      user = "testUser",
      roles = {"builder"})
  void testCreateAssociationSuccess() {
    UUID owningId = UUID.randomUUID();
    UUID foreignId = UUID.randomUUID();

    TopicAssociation mockAssoc = new TopicAssociation();
    // Set fields on mockAssoc if necessary for the JSON check

    when(associationService.createAssociation(owningId, foreignId)).thenReturn(mockAssoc);

    given()
        .queryParam("owningId", owningId.toString())
        .queryParam("foreignId", foreignId.toString())
        .when()
        .post("/topic-associations/create")
        .then()
        .statusCode(200);

    verify(associationService).createAssociation(owningId, foreignId);
  }

  @Test
  @TestSecurity(
      user = "testUser",
      roles = {"builder"})
  void testCreateAssociationValidationError() {
    // Missing query parameters should trigger a 400 due to @NotNull
    given().when().post("/topic-associations/create").then().statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testUser",
      roles = {"builder"})
  void testDeleteAssociationSuccess() {
    UUID assocId = UUID.randomUUID();

    doNothing().when(associationService).deleteAssociation(assocId);

    given()
        .pathParam("associationId", assocId)
        .when()
        .delete("/topic-associations/{associationId}")
        .then()
        .statusCode(200);

    verify(associationService).deleteAssociation(assocId);
  }

  @Test
  @TestSecurity(
      user = "testUser",
      roles = {"wrong_role"})
  void testAccessForbiddenForWrongRole() {
    given()
        .pathParam("associationId", UUID.randomUUID())
        .when()
        .delete("/topic-associations/{associationId}")
        .then()
        .statusCode(403);
  }

  @Test
  void testAccessUnauthorizedWithoutUser() {
    // No @TestSecurity annotation here simulates an unauthenticated request
    given()
        .pathParam("associationId", UUID.randomUUID())
        .when()
        .delete("/topic-associations/{associationId}")
        .then()
        .statusCode(401);
  }
}
