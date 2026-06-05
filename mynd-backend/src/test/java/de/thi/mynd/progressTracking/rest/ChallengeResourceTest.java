package de.thi.mynd.progressTracking.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.service.ChallengeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ChallengeResourceTest {

    @InjectMock
    ChallengeService challengeService;

    @Nested
    class SecurityTests {

        @Test
        void shouldReturn401WhenUnauthorized() {
            // No @TestSecurity annotation means the request is anonymous
            given()
                    .when()
                    .get("/challenges/current")
                    .then()
                    .statusCode(Status.UNAUTHORIZED.getStatusCode());
        }

        @Test
        @TestSecurity(user = "test-user", roles = "wrongRole")
        void shouldReturn403WhenRoleIsIncorrect() {
            given()
                    .when()
                    .get("/challenges/current")
                    .then()
                    .statusCode(Status.FORBIDDEN.getStatusCode());
        }
    }

    @Nested
    @TestSecurity(user = "test-user", roles = "authorizedUser")
    class AuthorizedEndpointTests {

        @Test
        void shouldReturnCurrentChallenge() {
            ChallengeDto mockDto = ChallengeDto.builder().build();
            mockDto.targetCount = 5;
            mockDto.completed = false;

            when(challengeService.getCurrentChallenge()).thenReturn(mockDto);

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/challenges/current")
                    .then()
                    .statusCode(Status.OK.getStatusCode())
                    .body("targetCount", is(5))
                    .body("completed", is(false));
        }

        @Test
        void shouldClaimRewardSuccessfully() {
            UUID challengeId = UUID.randomUUID();
            ChallengeDto mockDto = ChallengeDto.builder().build();
            mockDto.rewardClaimed = true;

            when(challengeService.claimReward(challengeId)).thenReturn(mockDto);

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", challengeId.toString())
                    .when()
                    .post("/challenges/claim/{id}")
                    .then()
                    .statusCode(Status.OK.getStatusCode())
                    .body("rewardClaimed", is(true));
        }

        @Test
        void shouldReturn400WhenClaimingRewardThrowsBadRequest() {
            UUID challengeId = UUID.randomUUID();

            when(challengeService.claimReward(challengeId))
                    .thenThrow(new BadRequestException("Reward already claimed"));

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", challengeId.toString())
                    .when()
                    .post("/challenges/claim/{id}")
                    .then()
                    // Quarkus maps JAX-RS WebApplicationExceptions (like BadRequestException)
                    // automatically to their respective HTTP status codes (400)
                    .statusCode(Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldReturnChallengeHistory() {
            ChallengeDto challenge1 = ChallengeDto.builder().build();
            challenge1.targetCount = 3;
            ChallengeDto challenge2 = ChallengeDto.builder().build();
            challenge2.targetCount = 7;

            when(challengeService.getChallengeHistory()).thenReturn(List.of(challenge1, challenge2));

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/challenges/history")
                    .then()
                    .statusCode(Status.OK.getStatusCode())
                    .body("size()", is(2))
                    .body("[0].targetCount", is(3))
                    .body("[1].targetCount", is(7));
        }
    }
}