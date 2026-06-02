package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PaymentServiceImplTest {

  @Inject PaymentService paymentService;

  @InjectMock SubscriptionService subscriptionService;

  @InjectMock StripeService stripeService;

  @InjectMock SecurityIdentity identity;

  @InjectMock MappingRegistry mappingRegistry;

  private static final String CREATOR_ID = "user-123";
  private static final String STRIPE_CUSTOMER_ID = "cus_abc123";

  @BeforeEach
  void setUp() {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(CREATOR_ID);
    when(identity.getPrincipal()).thenReturn(principal);
  }

  // --- createInitialSubscriptionSession ---

  @Test
  void
      createInitialSubscriptionSession_withFreeSubscriptionAndExistingCustomer_returnsSessionDto() {
    Subscription subscription = freeSubscriptionWithCustomer();
    Price price = mock(Price.class);
    Session session = mock(Session.class);
    StripeSessionDto expectedDto = StripeSessionDto.builder().build();

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(subscription);
    when(stripeService.obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM))
        .thenReturn(price);
    when(stripeService.createCheckoutSessionForSubscriptionPrice(price, STRIPE_CUSTOMER_ID))
        .thenReturn(session);
    when(mappingRegistry.map(session, StripeSessionDto.class)).thenReturn(expectedDto);

    StripeSessionDto result =
        paymentService.createInitialSubscriptionSession(SubscriptionStatus.PREMIUM);

    assertNotNull(result);
    assertSame(expectedDto, result);

    verify(stripeService, never()).getOrCreateCustomer(any());
    verify(subscriptionService, never()).updateCustomerId(any(), any());
    verify(stripeService).obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM);
    verify(stripeService).createCheckoutSessionForSubscriptionPrice(price, STRIPE_CUSTOMER_ID);
    verify(mappingRegistry).map(session, StripeSessionDto.class);
  }

  @Test
  void
      createInitialSubscriptionSession_withFreeSubscriptionAndNoCustomer_createsCustomerThenReturnsSessionDto() {
    Subscription subscriptionWithoutCustomer = freeSubscriptionWithoutCustomer();
    Subscription subscriptionWithCustomer = freeSubscriptionWithCustomer();
    Price price = mock(Price.class);
    Session session = mock(Session.class);
    StripeSessionDto expectedDto = StripeSessionDto.builder().build();

    com.stripe.model.Customer stripeCustomer = mock(com.stripe.model.Customer.class);
    when(stripeCustomer.getId()).thenReturn(STRIPE_CUSTOMER_ID);

    when(subscriptionService.getSubscriptionForCurrentUser())
        .thenReturn(subscriptionWithoutCustomer);
    when(stripeService.getOrCreateCustomer(CREATOR_ID)).thenReturn(stripeCustomer);
    when(subscriptionService.updateCustomerId(subscriptionWithoutCustomer, STRIPE_CUSTOMER_ID))
        .thenReturn(subscriptionWithCustomer);
    when(stripeService.obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM))
        .thenReturn(price);
    when(stripeService.createCheckoutSessionForSubscriptionPrice(price, STRIPE_CUSTOMER_ID))
        .thenReturn(session);
    when(mappingRegistry.map(session, StripeSessionDto.class)).thenReturn(expectedDto);

    StripeSessionDto result =
        paymentService.createInitialSubscriptionSession(SubscriptionStatus.PREMIUM);

    assertNotNull(result);
    assertSame(expectedDto, result);

    verify(stripeService).getOrCreateCustomer(CREATOR_ID);
    verify(subscriptionService).updateCustomerId(subscriptionWithoutCustomer, STRIPE_CUSTOMER_ID);
  }

  @Test
  void
      createInitialSubscriptionSession_withNonFreeSubscription_throwsCannotUpgradeSubscriptionException() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.PREMIUM);

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(subscription);

    CannotUpgradeSubscriptionException ex =
        assertThrows(
            CannotUpgradeSubscriptionException.class,
            () -> paymentService.createInitialSubscriptionSession(SubscriptionStatus.PREMIUM));

    assertEquals("You already have a subscription", ex.getMessage());

    verify(stripeService, never()).getOrCreateCustomer(any());
    verify(stripeService, never()).obtainPriceForSubscriptionStatus(any());
    verify(stripeService, never()).createCheckoutSessionForSubscriptionPrice(any(), any());
    verify(mappingRegistry, never()).map(any(), any());
  }

  @Test
  void createInitialSubscriptionSession_usesCurrentUserIdentityForCustomerLookup() {
    Subscription subscriptionWithoutCustomer = freeSubscriptionWithoutCustomer();
    Subscription subscriptionWithCustomer = freeSubscriptionWithCustomer();

    com.stripe.model.Customer stripeCustomer = mock(com.stripe.model.Customer.class);
    when(stripeCustomer.getId()).thenReturn(STRIPE_CUSTOMER_ID);

    when(subscriptionService.getSubscriptionForCurrentUser())
        .thenReturn(subscriptionWithoutCustomer);
    when(stripeService.getOrCreateCustomer(CREATOR_ID)).thenReturn(stripeCustomer);
    when(subscriptionService.updateCustomerId(subscriptionWithoutCustomer, STRIPE_CUSTOMER_ID))
        .thenReturn(subscriptionWithCustomer);
    when(stripeService.obtainPriceForSubscriptionStatus(any())).thenReturn(mock(Price.class));
    when(stripeService.createCheckoutSessionForSubscriptionPrice(any(), any()))
        .thenReturn(mock(Session.class));
    when(mappingRegistry.map(any(), eq(StripeSessionDto.class)))
        .thenReturn(StripeSessionDto.builder().build());

    paymentService.createInitialSubscriptionSession(SubscriptionStatus.PREMIUM);

    verify(identity).getPrincipal();
    verify(stripeService).getOrCreateCustomer(CREATOR_ID);
  }

  // --- Helpers ---

  private Subscription freeSubscriptionWithCustomer() {
    Subscription s = new Subscription();
    s.subscriptionStatus = SubscriptionStatus.FREE;
    s.stripeCustomerId = STRIPE_CUSTOMER_ID;
    return s;
  }

  private Subscription freeSubscriptionWithoutCustomer() {
    Subscription s = new Subscription();
    s.subscriptionStatus = SubscriptionStatus.FREE;
    s.stripeCustomerId = null;
    return s;
  }

  private Subscription subscriptionWithStatus(SubscriptionStatus status) {
    Subscription s = new Subscription();
    s.subscriptionStatus = status;
    s.stripeCustomerId = STRIPE_CUSTOMER_ID;
    return s;
  }
}
