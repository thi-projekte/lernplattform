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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.thi.mynd.subscription.dto.PriceDto;
import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.request.SubscribeRequest;
import de.thi.mynd.subscription.service.PaymentService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PaymentResourceTest {

  @InjectMock PaymentService paymentService;

  private SubscribeRequest subscribeRequest(String priceId) {
    SubscribeRequest request = new SubscribeRequest();
    request.priceId = priceId;
    return request;
  }

  // --- POST /payments/subscribe ---

  @Test
  void createSubscriptionSession_whenAnonymous_thenIsUnauthorized() {
    given()
        .contentType(ContentType.JSON)
        .body(subscribeRequest("price_123"))
        .when()
        .post("/payments/subscribe")
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void createSubscriptionSession_whenAuthorized_returnsSessionDto() {
    StripeSessionDto dto =
        StripeSessionDto.builder().url("https://checkout.stripe.com/abc").build();
    when(paymentService.createInitialSubscriptionSession("price_123")).thenReturn(dto);

    given()
        .contentType(ContentType.JSON)
        .body(subscribeRequest("price_123"))
        .when()
        .post("/payments/subscribe")
        .then()
        .statusCode(200)
        .body("url", is("https://checkout.stripe.com/abc"));
  }

  // --- GET /payments/products ---

  @Test
  void getProducts_whenAnonymous_thenIsUnauthorized() {
    given().when().get("/payments/products").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void getProducts_whenAuthorized_returnsProductList() {
    ProductDto dto =
        ProductDto.builder()
            .title("Premium")
            .subscriptionStatus(SubscriptionStatus.PREMIUM)
            .canHaveTrial(true)
            .prices(
                List.of(PriceDto.builder().id("price_1").interval("month").amount(9.99).build()))
            .features(Collections.emptyList())
            .build();
    when(paymentService.getAllProducts()).thenReturn(List.of(dto));

    given()
        .when()
        .get("/payments/products")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(1))
        .body("[0].title", is("Premium"));
  }

  // --- POST /payments/trial ---

  @Test
  void createTrial_whenAnonymous_thenIsUnauthorized() {
    given()
        .contentType(ContentType.JSON)
        .body(subscribeRequest("price_123"))
        .when()
        .post("/payments/trial")
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void createTrial_whenAuthorized_returns200AndDelegatesToService() {
    given()
        .contentType(ContentType.JSON)
        .body(subscribeRequest("price_123"))
        .when()
        .post("/payments/trial")
        .then()
        .statusCode(200);

    verify(paymentService).createTrial(eq("price_123"));
  }
}
