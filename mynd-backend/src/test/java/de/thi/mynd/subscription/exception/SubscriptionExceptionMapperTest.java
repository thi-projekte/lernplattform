/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.exception;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * {@link SubscriptionExceptionMapper} is a plain {@code @ServerExceptionMapper} holder, not a CDI
 * bean, so it is instantiated directly rather than injected.
 */
@QuarkusTest
class SubscriptionExceptionMapperTest {

  private final SubscriptionExceptionMapper subscriptionExceptionMapper =
      new SubscriptionExceptionMapper();

  @Test
  void mapCannotUpgradeSubscriptionException_returnsConflict() {
    Response response =
        subscriptionExceptionMapper.mapCannotUpgradeSubscriptionException(
            new CannotUpgradeSubscriptionException("cannot upgrade"));

    assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    assertEquals("cannot upgrade", response.getEntity());
  }

  @Test
  void mapProductNotFoundException_returnsInternalServerError() {
    Response response =
        subscriptionExceptionMapper.mapProductNotFoundException(
            new ProductNotFoundException("no product"));

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals("no product", response.getEntity());
  }

  @Test
  void mapHandledStripeException_returnsInternalServerError() {
    Response response =
        subscriptionExceptionMapper.mapHandledStripeException(
            new HandledStripeException("stripe error"));

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals("stripe error", response.getEntity());
  }

  @Test
  void mapInvalidStripeSignatureException_returnsBadRequest() {
    Response response =
        subscriptionExceptionMapper.mapInvalidStripeSignatureException(
            new InvalidStripeSignatureException("bad signature"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("bad signature", response.getEntity());
  }

  @Test
  void mapStripeCustomerAlreadyExists_returnsBadRequest() {
    Response response =
        subscriptionExceptionMapper.mapStripeCustomerAlreadyExists(
            new StripeCustomerAlreadyExists("customer exists"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("customer exists", response.getEntity());
  }

  @Test
  void mapSubscriptionNotFoundException_returnsBadRequest() {
    Response response =
        subscriptionExceptionMapper.mapSubscriptionNotFoundException(
            new SubscriptionNotFoundException("no subscription"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("no subscription", response.getEntity());
  }

  @Test
  void mapFeatureQuotaHitException_returnsConflict() {
    Response response =
        subscriptionExceptionMapper.mapFeatureQuotaHitException(
            new FeatureQuotaHitException("quota hit"));

    assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    assertEquals("quota hit", response.getEntity());
  }
}
