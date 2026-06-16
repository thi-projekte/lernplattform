/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.auth.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.*;

import de.thi.mynd.auth.service.AuthService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AuthResourceTest {

  @InjectMock AuthService authService;

  @Test
  @TestSecurity(user = "test-user", roles = "authorizedUser")
  void testCheckUserIsBuilder_True() {
    // Mocking: Service sagt, User ist ein Builder
    when(authService.checkUserIsBuilder("test-user")).thenReturn(true);

    given().when().get("/auth/check-is-builder").then().statusCode(200); // Response.ok()

    verify(authService).checkUserIsBuilder("test-user");
  }

  @Test
  @TestSecurity(user = "test-user", roles = "authorizedUser")
  void testCheckUserIsBuilder_False() {
    // Mocking: Service sagt, User ist KEIN Builder
    when(authService.checkUserIsBuilder("test-user")).thenReturn(false);

    given().when().get("/auth/check-is-builder").then().statusCode(204); // Response.noContent()
  }

  @Test
  @TestSecurity(user = "builder-candidate", roles = "authorizedUser")
  void testMakeUserBuilder_Success() {
    // Hier rufen wir POST auf
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/auth/register-as-builder")
        .then()
        .statusCode(201); // Response.status(201)

    // Verifizieren, dass der Service-Call mit dem richtigen Namen ankam
    verify(authService).makeUserABuilder("builder-candidate");
  }

  @Test
  @TestSecurity(user = "builder-candidate", roles = "authorizedUser")
  void testMakeUserLearner_Success() {
    // Hier rufen wir POST auf
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/auth/register-as-learner")
        .then()
        .statusCode(201); // Response.status(201)

    // Verifizieren, dass der Service-Call mit dem richtigen Namen ankam
    verify(authService).makeUserALearner("builder-candidate");
  }
}
