/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.thi.mynd.subscription.dto.LimitsDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.service.FeatureQuotaRetrievalService;
import de.thi.mynd.subscription.service.SubscriptionService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SubscriptionResourceTest {

  @InjectMock SubscriptionService subscriptionService;

  @InjectMock FeatureQuotaRetrievalService featureQuotaRetrievalService;

  // --- GET /subscriptions/limits/free ---

  @Test
  void getLimitsForPlan_whenAnonymous_thenIsUnauthorized() {
    given().when().get("/subscriptions/limits/free").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void getLimitsForPlan_whenAuthorized_returnsLimitsDto() {
    LimitsDto dto = LimitsDto.builder().dailyLearningLimit(3).parallelTopics(2).build();
    when(featureQuotaRetrievalService.getLimitForFreePlan()).thenReturn(dto);

    given()
        .when()
        .get("/subscriptions/limits/free")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("dailyLearningLimit", is(3))
        .body("parallelTopics", is(2));
  }

  // --- GET /subscriptions ---

  @Test
  void getSubscription_whenAnonymous_thenIsUnauthorized() {
    given().when().get("/subscriptions").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void getSubscription_whenAuthorized_returnsSubscriptionDto() {
    SubscriptionDto dto =
        SubscriptionDto.builder()
            .creatorId("alice")
            .subscriptionStatus(SubscriptionStatus.FREE)
            .canAccessBillingPortal(false)
            .canLearnTopics(true)
            .canStartNewTopics(true)
            .build();
    when(subscriptionService.getSubscriptionForCurrentUserAsDto()).thenReturn(dto);

    given()
        .when()
        .get("/subscriptions")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("creatorId", is("alice"))
        .body("subscriptionStatus", is("FREE"));
  }

  // --- POST /subscriptions/billing-portal-session ---

  @Test
  void createBillingPortalSession_whenAnonymous_thenIsUnauthorized() {
    given().when().post("/subscriptions/billing-portal-session").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void createBillingPortalSession_whenAuthorized_returnsSessionDto() {
    StripeSessionDto dto =
        StripeSessionDto.builder().url("https://billing.stripe.com/p/abc").build();
    when(subscriptionService.createBillingPortalSession()).thenReturn(dto);

    given()
        .when()
        .post("/subscriptions/billing-portal-session")
        .then()
        .statusCode(200)
        .body("url", is("https://billing.stripe.com/p/abc"));
  }
}
