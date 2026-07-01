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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import de.thi.mynd.subscription.exception.InvalidStripeSignatureException;
import de.thi.mynd.subscription.service.StripeWebhookService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StripeWebhookResourceTest {

  @InjectMock StripeWebhookService stripeWebhookService;

  private static final String PAYLOAD = "{\"id\":\"evt_123\",\"type\":\"customer.created\"}";
  private static final String SIGNATURE = "t=1,v1=abcdef";

  @Test
  void handleWebhook_anonymousRequest_isNotUnauthorized() {
    given()
        .contentType(ContentType.TEXT)
        .header("Stripe-Signature", SIGNATURE)
        .body(PAYLOAD)
        .when()
        .post("/stripe-webhook")
        .then()
        .statusCode(200);
  }

  @Test
  void handleWebhook_serviceSucceeds_returns200AndDelegatesToService() {
    given()
        .contentType(ContentType.TEXT)
        .header("Stripe-Signature", SIGNATURE)
        .body(PAYLOAD)
        .when()
        .post("/stripe-webhook")
        .then()
        .statusCode(200);

    verify(stripeWebhookService).processWebhook(eq(PAYLOAD), eq(SIGNATURE));
  }

  @Test
  void handleWebhook_invalidSignature_returns400() {
    doThrow(new InvalidStripeSignatureException("Invalid signature"))
        .when(stripeWebhookService)
        .processWebhook(eq(PAYLOAD), eq(SIGNATURE));

    given()
        .contentType(ContentType.TEXT)
        .header("Stripe-Signature", SIGNATURE)
        .body(PAYLOAD)
        .when()
        .post("/stripe-webhook")
        .then()
        .statusCode(400);
  }
}
