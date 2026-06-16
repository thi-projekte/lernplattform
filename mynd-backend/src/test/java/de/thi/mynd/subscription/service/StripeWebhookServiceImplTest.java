/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.InvalidStripeSignatureException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@QuarkusTest
class StripeWebhookServiceImplTest {

  @Inject StripeWebhookService stripeWebhookService;

  @InjectMock SubscriptionService subscriptionService;

  @InjectMock StripeService stripeService;

  private static final String PAYLOAD = "{}";
  private static final String SIG_HEADER = "t=123,v1=abc";
  private static final String CUSTOMER_ID = "cus_abc123";
  private static final String SUBSCRIPTION_ID = "sub_xyz";
  private static final String PRODUCT_ID = "prod_123";
  private static final String TIER = "PREMIUM";

  private MockedStatic<Webhook> webhookMock;

  @BeforeEach
  void setUp() {
    webhookMock = mockStatic(Webhook.class);
  }

  @AfterEach
  void tearDown() {
    webhookMock.close();
  }

  // --- verifySignatureAndExtractEvent ---

  @Test
  void processWebhook_withInvalidSignature_throwsInvalidStripeSignatureException()
      throws Exception {
    webhookMock
        .when(() -> Webhook.constructEvent(eq(PAYLOAD), eq(SIG_HEADER), any()))
        .thenThrow(mock(SignatureVerificationException.class));

    assertThrows(
        InvalidStripeSignatureException.class,
        () -> stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER));
  }

  @Test
  void processWebhook_withUnknownEventType_doesNothing() throws Exception {
    Event event = mockEvent("some.unknown.event", null);
    stubWebhook(event);

    assertDoesNotThrow(() -> stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER));

    verifyNoInteractions(subscriptionService, stripeService);
  }

  // --- customer.subscription.created ---

  @Test
  void processWebhook_subscriptionCreated_setsSubscriptionIdAndStatus() throws Exception {
    Subscription stripeSubscription = buildStripeSubscription(false);
    Event event = mockEvent("customer.subscription.created", stripeSubscription);
    stubWebhook(event);

    Product product = mockProductWithTier(TIER);
    when(stripeService.getFullProductById(PRODUCT_ID)).thenReturn(product);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verify(subscriptionService)
        .setSubscriptionIdAndStatusForCustomerId(
            eq(CUSTOMER_ID), eq(SUBSCRIPTION_ID), eq(SubscriptionStatus.PREMIUM), anyList());
    verify(subscriptionService, never())
        .setSubscriptionStatusForSubscriptionId(any(), any(), any());
  }

  @Test
  void processWebhook_subscriptionCreated_withEmptyDeserializedObject_doesNothing()
      throws Exception {
    Event event = mockEventWithEmptyObject("customer.subscription.created");
    stubWebhook(event);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verifyNoInteractions(subscriptionService, stripeService);
  }

  @Test
  void processWebhook_subscriptionCreated_withNoSubscriptionItems_throwsProductNotFoundException()
      throws Exception {
    Subscription stripeSubscription = buildStripeSubscriptionWithNoItems();
    Event event = mockEvent("customer.subscription.created", stripeSubscription);
    stubWebhook(event);

    assertThrows(
        ProductNotFoundException.class,
        () -> stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER));
  }

  // --- customer.subscription.updated ---

  @Test
  void processWebhook_subscriptionUpdated_updatesSubscriptionStatus() throws Exception {
    Subscription stripeSubscription = buildStripeSubscription(false);
    Event event = mockEvent("customer.subscription.updated", stripeSubscription);
    stubWebhook(event);

    Product product = mockProductWithTier(TIER);
    when(stripeService.getFullProductById(PRODUCT_ID)).thenReturn(product);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verify(subscriptionService)
        .setSubscriptionStatusForSubscriptionId(
            eq(SUBSCRIPTION_ID), eq(SubscriptionStatus.PREMIUM), anyList());
    verify(subscriptionService, never())
        .setSubscriptionIdAndStatusForCustomerId(any(), any(), any(), any());
  }

  @Test
  void processWebhook_subscriptionUpdated_withCancelAtPeriodEnd_skipsStatusUpdate()
      throws Exception {
    Subscription stripeSubscription = buildStripeSubscription(true);
    Event event = mockEvent("customer.subscription.updated", stripeSubscription);
    stubWebhook(event);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verifyNoInteractions(subscriptionService, stripeService);
  }

  @Test
  void processWebhook_subscriptionUpdated_withEmptyDeserializedObject_doesNothing()
      throws Exception {
    Event event = mockEventWithEmptyObject("customer.subscription.updated");
    stubWebhook(event);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verifyNoInteractions(subscriptionService, stripeService);
  }

  @Test
  void processWebhook_subscriptionUpdated_withNoSubscriptionItems_throwsProductNotFoundException()
      throws Exception {
    Subscription stripeSubscription = buildStripeSubscriptionWithNoItems();
    Event event = mockEvent("customer.subscription.updated", stripeSubscription);
    stubWebhook(event);

    assertThrows(
        ProductNotFoundException.class,
        () -> stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER));
  }

  // --- customer.subscription.deleted ---

  @Test
  void processWebhook_subscriptionDeleted_setsStatusToFree() throws Exception {
    Subscription stripeSubscription = buildStripeSubscription(false);
    Event event = mockEvent("customer.subscription.deleted", stripeSubscription);
    stubWebhook(event);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verify(subscriptionService)
        .setSubscriptionStatusForSubscriptionId(
            eq(SUBSCRIPTION_ID), eq(SubscriptionStatus.FREE), anyList());
    verifyNoInteractions(stripeService);
  }

  @Test
  void processWebhook_subscriptionDeleted_withEmptyDeserializedObject_doesNothing()
      throws Exception {
    Event event = mockEventWithEmptyObject("customer.subscription.deleted");
    stubWebhook(event);

    stripeWebhookService.processWebhook(PAYLOAD, SIG_HEADER);

    verifyNoInteractions(subscriptionService, stripeService);
  }

  // --- Helpers ---

  private void stubWebhook(Event event) throws Exception {
    webhookMock
        .when(() -> Webhook.constructEvent(eq(PAYLOAD), eq(SIG_HEADER), any()))
        .thenReturn(event);
  }

  /** Builds an Event whose deserializer returns the given Stripe Subscription object. */
  private Event mockEvent(String type, Subscription stripeSubscription) {
    Event event = mock(Event.class);
    when(event.getType()).thenReturn(type);

    EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
    when(event.getDataObjectDeserializer()).thenReturn(deserializer);
    when(deserializer.getObject()).thenReturn(Optional.ofNullable(stripeSubscription));

    return event;
  }

  /** Builds an Event whose deserializer returns an empty Optional. */
  private Event mockEventWithEmptyObject(String type) {
    Event event = mock(Event.class);
    when(event.getType()).thenReturn(type);

    EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
    when(event.getDataObjectDeserializer()).thenReturn(deserializer);
    when(deserializer.getObject()).thenReturn(Optional.empty());

    return event;
  }

  /** Builds a Stripe Subscription with one line item pointing to PRODUCT_ID. */
  private Subscription buildStripeSubscription(boolean cancelAtPeriodEnd) {
    Subscription subscription = mock(Subscription.class);
    when(subscription.getId()).thenReturn(SUBSCRIPTION_ID);
    when(subscription.getCustomer()).thenReturn(CUSTOMER_ID);
    when(subscription.getCancelAtPeriodEnd()).thenReturn(cancelAtPeriodEnd);

    Price price = mock(Price.class);
    when(price.getProduct()).thenReturn(PRODUCT_ID);

    SubscriptionItem item = mock(SubscriptionItem.class);
    when(item.getPrice()).thenReturn(price);

    SubscriptionItemCollection itemCollection = mock(SubscriptionItemCollection.class);
    when(itemCollection.getData()).thenReturn(List.of(item));
    when(subscription.getItems()).thenReturn(itemCollection);

    return subscription;
  }

  /** Builds a Stripe Subscription with an empty items list. */
  private Subscription buildStripeSubscriptionWithNoItems() {
    Subscription subscription = mock(Subscription.class);
    when(subscription.getId()).thenReturn(SUBSCRIPTION_ID);
    when(subscription.getCustomer()).thenReturn(CUSTOMER_ID);
    when(subscription.getCancelAtPeriodEnd()).thenReturn(false);

    SubscriptionItemCollection itemCollection = mock(SubscriptionItemCollection.class);
    when(itemCollection.getData()).thenReturn(List.of());
    when(subscription.getItems()).thenReturn(itemCollection);

    return subscription;
  }

  private Product mockProductWithTier(String tier) {
    Product product = mock(Product.class);
    when(product.getMetadata()).thenReturn(Map.of("tier", tier));
    return product;
  }
}
