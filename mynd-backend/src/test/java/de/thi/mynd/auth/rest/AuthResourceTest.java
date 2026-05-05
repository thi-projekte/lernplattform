package de.thi.mynd.auth.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.thi.mynd.auth.RegisterRole;
import de.thi.mynd.auth.dto.CheckUsernameExistsRequestDto;
import de.thi.mynd.auth.dto.RegisterUserRequestDto;
import de.thi.mynd.auth.exception.UserAlreadyExistsException;
import de.thi.mynd.auth.service.AuthService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AuthResourceTest {

  @InjectMock AuthService authService;

  @Test
  void testCheckUserExists_Returns200() {
    CheckUsernameExistsRequestDto dto = new CheckUsernameExistsRequestDto();
    dto.username = "existingUser";

    when(authService.checkUsernameExists("existingUser")).thenReturn(true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/auth/check-existance")
        .then()
        .statusCode(200);
  }

  @Test
  void testCheckUserExists_Returns204() {
    CheckUsernameExistsRequestDto dto = new CheckUsernameExistsRequestDto();
    dto.username = "unknownUser";

    when(authService.checkUsernameExists("unknownUser")).thenReturn(false);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/auth/check-existance")
        .then()
        .statusCode(204);
  }

  @Test
  void testCreateUserAccount_Returns201() throws UserAlreadyExistsException {
    RegisterUserRequestDto dto = new RegisterUserRequestDto();
    dto.username = "newuser";
    dto.email = "a@a.a";
    dto.password = "abcdef";
    dto.firstName = "max";
    dto.lastName = "mustermann";
    dto.role = RegisterRole.Builder;

    // authService.registerUser(dto) returns void, so no 'when' needed
    // unless we want to verify behavior.

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/auth/register")
        .then()
        .statusCode(201);
  }

  @Test
  void testCreateUserAccount_Returns400_WhenUserExists() throws UserAlreadyExistsException {
    RegisterUserRequestDto dto = new RegisterUserRequestDto();
    dto.username = "duplicate";
    dto.email = "a@a.a";
    dto.password = "abcdef";
    dto.firstName = "max";
    dto.lastName = "mustermann";

    // Mock the service throwing the custom exception
    doThrow(new UserAlreadyExistsException("Exists"))
        .when(authService)
        .registerUser(any(RegisterUserRequestDto.class));

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/auth/register")
        .then()
        .statusCode(400);
  }
}
