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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.service.UserProfileService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ProfilePictureResourceTest {

  @InjectMock UserProfileService userProfileService;

  private ProfilePictureDto mockDto;

  @BeforeEach
  void setUp() {
    mockDto = new ProfilePictureDto("");
  }

  // ==========================================
  // POST /auth/profile-picture Tests
  // ==========================================

  @Test
  @TestSecurity(user = "user-jane", roles = "authorizedUser")
  void shouldUploadProfilePictureSuccessfully() {
    // Arrange
    when(userProfileService.uploadProfilePicture(eq("user-jane"), any(FileUpload.class)))
        .thenReturn(mockDto);

    // Act & Assert
    given()
        // Simulates a multipart form transmission containing a small mock text file payload
        .multiPart("file", "avatar.jpg", "fake-image-bytes".getBytes(), "image/jpeg")
        .when()
        .post("/auth/profile-picture")
        .then()
        .statusCode(201);

    Mockito.verify(userProfileService, Mockito.times(1))
        .uploadProfilePicture(eq("user-jane"), any(FileUpload.class));
  }

  @Test
  void shouldReturnUnauthorizedWhenUploadingWithoutSession() {
    given()
        .multiPart("file", "avatar.jpg", "bytes".getBytes())
        .when()
        .post("/auth/profile-picture")
        .then()
        .statusCode(401);
  }

  /*
   NOTE ON EXCEPTION TESTING:
   Because validation like file sizes (413) or bad formats (415) typically live inside
   your service class, you mock those behaviors by telling your service mock to throw
   whatever exception maps to those status codes in your ExceptionMappers.
  */
  @Test
  @TestSecurity(user = "user-jane", roles = "authorizedUser")
  void shouldReturnUnsupportedMediaTypeWhenServiceRejectsFileType() {
    // Arrange: Simulate your service throwing an exception mapped to 415 Unsupported Media Type
    when(userProfileService.uploadProfilePicture(eq("user-jane"), any(FileUpload.class)))
        .thenThrow(new WebApplicationException("Invalid file type", 415));

    // Act & Assert
    given()
        .multiPart("file", "malicious.exe", "exe-bytes".getBytes(), "application/x-msdownload")
        .when()
        .post("/auth/profile-picture")
        .then()
        .statusCode(415);
  }

  // ==========================================
  // DELETE /auth/profile-picture Tests
  // ==========================================

  @Test
  @TestSecurity(user = "user-jane", roles = "authorizedUser")
  void shouldDeleteProfilePictureSuccessfully() {
    // Act & Assert
    given().when().delete("/auth/profile-picture").then().statusCode(200);

    Mockito.verify(userProfileService, Mockito.times(1)).deleteProfilePicture("user-jane");
  }

  @Test
  @TestSecurity(user = "user-jane", roles = "authorizedUser")
  void shouldReturnNotFoundWhenDeletingNonExistentPicture() {
    // Arrange
    Mockito.doThrow(new WebApplicationException("No profile picture found", 404))
        .when(userProfileService)
        .deleteProfilePicture("user-jane");

    // Act & Assert
    given().when().delete("/auth/profile-picture").then().statusCode(404);
  }

  // ==========================================
  // GET /auth/profile-picture/{username} Tests
  // ==========================================

  @Test
  @TestSecurity(user = "any-authenticated-user", roles = "authorizedUser")
  void shouldReturnProfilePictureDataForGivenUser() {
    // Arrange
    when(userProfileService.getProfilePicture("target-user")).thenReturn(mockDto);

    // Act & Assert
    given()
        .pathParam("username", "target-user")
        .when()
        .get("/auth/profile-picture/{username}")
        .then()
        .statusCode(200);
    // If your DTO yields json keys, assert against them here:
    // .body("url", equalTo("https://s3.storage.com/profiles/avatar.jpg"));
  }

  @Test
  @TestSecurity(user = "any-authenticated-user", roles = "authorizedUser")
  void shouldReturnNotFoundWhenTargetUserHasNoPicture() {
    // Arrange
    when(userProfileService.getProfilePicture("ghost-user"))
        .thenThrow(new WebApplicationException("Not Found", 404));

    // Act & Assert
    given()
        .pathParam("username", "ghost-user")
        .when()
        .get("/auth/profile-picture/{username}")
        .then()
        .statusCode(404);
  }
}
