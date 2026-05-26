package de.thi.mynd.progressTracking.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.StreakType;
import de.thi.mynd.progressTracking.request.StreakPreferenceRequest;
import de.thi.mynd.progressTracking.service.StreakService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StreakResourceTest {

  @InjectMock StreakService streakService;

  // =========================================================================
  // GET /streaks
  // =========================================================================

  @Nested
  class GetStreaks {

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void returns200_withStreakList() {
      StreakDto dto = StreakDto.builder().build();
      dto.type = StreakType.DAILY;

      when(streakService.getLatestStreaksForCurrentUser()).thenReturn(List.of(dto));

      given()
          .when()
          .get("/streaks")
          .then()
          .statusCode(200)
          .body("$", hasSize(1))
          .body("[0].type", equalTo("DAILY"));
    }

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void returns200_withEmptyList_whenNoStreaks() {
      when(streakService.getLatestStreaksForCurrentUser()).thenReturn(List.of());

      given().when().get("/streaks").then().statusCode(200).body("$", hasSize(0));
    }

    @Test
    void returns401_whenNotAuthenticated() {
      given().when().get("/streaks").then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "testUser", roles = "wrongRole")
    void returns403_whenWrongRole() {
      given().when().get("/streaks").then().statusCode(403);
    }
  }

  // =========================================================================
  // GET /streaks/preferences
  // =========================================================================

  @Nested
  class GetStreakPreferences {

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void returns200_withPreference() {
      StreakPreferenceDto dto = StreakPreferenceDto.builder().build();
      dto.type = StreakType.WEEKLY;
      dto.isPublic = true;

      when(streakService.getOrCreateStreakPreferenceForCurrentUser()).thenReturn(dto);

      given()
          .when()
          .get("/streaks/preferences")
          .then()
          .statusCode(200)
          .body("type", equalTo("WEEKLY"))
          .body("isPublic", equalTo(true));
    }

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void createsDefaultPreference_whenNoneExists() {
      StreakPreferenceDto defaultDto = StreakPreferenceDto.builder().build();
      defaultDto.type = StreakType.DAILY;
      defaultDto.isPublic = false;

      when(streakService.getOrCreateStreakPreferenceForCurrentUser()).thenReturn(defaultDto);

      given()
          .when()
          .get("/streaks/preferences")
          .then()
          .statusCode(200)
          .body("type", equalTo("DAILY"))
          .body("isPublic", equalTo(false));

      verify(streakService).getOrCreateStreakPreferenceForCurrentUser();
    }

    @Test
    void returns401_whenNotAuthenticated() {
      given().when().get("/streaks/preferences").then().statusCode(401);
    }
  }

  // =========================================================================
  // PUT /streaks/preferences
  // =========================================================================

  @Nested
  class UpdateStreakPreferences {

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void returns200_onValidRequest() {
      doNothing().when(streakService).updateStreakPreferencesForCurrentUser(any());

      given()
          .contentType(ContentType.JSON)
          .body(
              """
                    { "type": "MONTHLY", "isPublic": true }
                    """)
          .when()
          .put("/streaks/preferences")
          .then()
          .statusCode(200);

      verify(streakService)
          .updateStreakPreferencesForCurrentUser(any(StreakPreferenceRequest.class));
    }

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void returns400_whenBodyIsMissing() {
      given()
          .contentType(ContentType.JSON)
          .when()
          .put("/streaks/preferences")
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void callsServiceWithCorrectValues() {
      doNothing().when(streakService).updateStreakPreferencesForCurrentUser(any());

      given()
          .contentType(ContentType.JSON)
          .body(
              """
                    { "type": "WEEKLY", "isPublic": false }
                    """)
          .when()
          .put("/streaks/preferences")
          .then()
          .statusCode(200);

      verify(streakService)
          .updateStreakPreferencesForCurrentUser(
              argThat(req -> req.type == StreakType.WEEKLY && !req.isPublic));
    }

    @Test
    void returns401_whenNotAuthenticated() {
      given()
          .contentType(ContentType.JSON)
          .body(
              """
                    { "type": "DAILY", "isPublic": false }
                    """)
          .when()
          .put("/streaks/preferences")
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testUser", roles = "authorizedUser")
    void returns500_whenServiceThrows() {
      doThrow(new RuntimeException("Unexpected error"))
          .when(streakService)
          .updateStreakPreferencesForCurrentUser(any());

      given()
          .contentType(ContentType.JSON)
          .body(
              """
                    { "type": "DAILY", "isPublic": false }
                    """)
          .when()
          .put("/streaks/preferences")
          .then()
          .statusCode(500);
    }
  }
}
