/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.List;
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
    String priceId = "price_123";
    Session session = mock(Session.class);
    StripeSessionDto expectedDto = StripeSessionDto.builder().build();

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(subscription);
    when(stripeService.createCheckoutSessionForSubscriptionPrice(priceId, STRIPE_CUSTOMER_ID))
        .thenReturn(session);
    when(mappingRegistry.map(session, StripeSessionDto.class)).thenReturn(expectedDto);

    StripeSessionDto result = paymentService.createInitialSubscriptionSession(priceId);

    assertNotNull(result);
    assertSame(expectedDto, result);

    verify(stripeService, never()).getOrCreateCustomer(any());
    verify(subscriptionService, never()).updateCustomerId(any(), any());
    verify(stripeService).createCheckoutSessionForSubscriptionPrice(priceId, STRIPE_CUSTOMER_ID);
    verify(mappingRegistry).map(session, StripeSessionDto.class);
  }

  @Test
  void
      createInitialSubscriptionSession_withFreeSubscriptionAndNoCustomer_createsCustomerThenReturnsSessionDto() {
    Subscription subscriptionWithoutCustomer = freeSubscriptionWithoutCustomer();
    Subscription subscriptionWithCustomer = freeSubscriptionWithCustomer();
    String priceId = "price_123";
    Session session = mock(Session.class);
    StripeSessionDto expectedDto = StripeSessionDto.builder().build();

    com.stripe.model.Customer stripeCustomer = mock(com.stripe.model.Customer.class);
    when(stripeCustomer.getId()).thenReturn(STRIPE_CUSTOMER_ID);

    when(subscriptionService.getSubscriptionForCurrentUser())
        .thenReturn(subscriptionWithoutCustomer);
    when(stripeService.getOrCreateCustomer(CREATOR_ID)).thenReturn(stripeCustomer);
    when(subscriptionService.updateCustomerId(subscriptionWithoutCustomer, STRIPE_CUSTOMER_ID))
        .thenReturn(subscriptionWithCustomer);
    when(stripeService.createCheckoutSessionForSubscriptionPrice(priceId, STRIPE_CUSTOMER_ID))
        .thenReturn(session);
    when(mappingRegistry.map(session, StripeSessionDto.class)).thenReturn(expectedDto);

    StripeSessionDto result = paymentService.createInitialSubscriptionSession(priceId);

    assertNotNull(result);
    assertSame(expectedDto, result);

    verify(stripeService).getOrCreateCustomer(CREATOR_ID);
    verify(subscriptionService).updateCustomerId(subscriptionWithoutCustomer, STRIPE_CUSTOMER_ID);
  }

  @Test
  void
      createInitialSubscriptionSession_withNonFreeSubscription_throwsCannotUpgradeSubscriptionException() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.PREMIUM);
    String priceId = "price_123";

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(subscription);

    CannotUpgradeSubscriptionException ex =
        assertThrows(
            CannotUpgradeSubscriptionException.class,
            () -> paymentService.createInitialSubscriptionSession(priceId));

    assertEquals("You already have a subscription", ex.getMessage());

    verify(stripeService, never()).getOrCreateCustomer(any());
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
    when(stripeService.createCheckoutSessionForSubscriptionPrice(any(), any()))
        .thenReturn(mock(Session.class));
    when(mappingRegistry.map(any(), eq(StripeSessionDto.class)))
        .thenReturn(StripeSessionDto.builder().build());

    paymentService.createInitialSubscriptionSession("price_123");

    verify(identity).getPrincipal();
    verify(stripeService).getOrCreateCustomer(CREATOR_ID);
  }

  @Test
  void testGetAllProducts_Success() {
    // 1. Arrange Subscription
    Subscription mockSubscription = new Subscription();
    mockSubscription.usedTrial = true; // Setting the flag to true
    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(mockSubscription);

    // 2. Arrange Stripe Products
    Product mockProduct = new Product();
    mockProduct.setId("prod_123");
    List<Product> mockProductList = List.of(mockProduct);
    when(stripeService.getAllProductsWithPricesAndMetaData()).thenReturn(mockProductList);

    // 3. Arrange Mapping Registry
    ProductDto mockDto = ProductDto.builder().build();
    List<ProductDto> expectedDtoList = List.of(mockDto);

    // Match the exact parameters your method passes down
    when(mappingRegistry.mapList(mockProductList, ProductDto.class, true))
        .thenReturn(expectedDtoList);

    // Act
    List<ProductDto> result = paymentService.getAllProducts();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());

    // Verify the interactions happened exactly as expected
    verify(subscriptionService, times(1)).getSubscriptionForCurrentUser();
    verify(stripeService, times(1)).getAllProductsWithPricesAndMetaData();
    verify(mappingRegistry, times(1)).mapList(mockProductList, ProductDto.class, true);
  }

  @Test
  void testGetAllProducts_PropagatesExceptionWhenSubscriptionFails() {
    // Arrange
    when(subscriptionService.getSubscriptionForCurrentUser())
        .thenThrow(new RuntimeException("User not authenticated"));

    // Act & Assert
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              paymentService.getAllProducts();
            });

    assertEquals("User not authenticated", exception.getMessage());

    // Verify downstream services were never called because it failed early
    verifyNoInteractions(stripeService);
    verifyNoInteractions(mappingRegistry);
  }

  @Test
  void testCreateTrial_Success_WithExistingStripeCustomer() {
    // Arrange
    String priceId = "price_premium";

    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = CREATOR_ID;

    Subscription mockSubscription = new Subscription();
    mockSubscription.id = id;
    mockSubscription.usedTrial = false;
    mockSubscription.subscriptionStatus = SubscriptionStatus.FREE;
    mockSubscription.stripeCustomerId = "cus_already_exists";

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(mockSubscription);

    // Act
    paymentService.createTrial(priceId);

    // Assert
    assertTrue(mockSubscription.usedTrial);
    verify(subscriptionService, times(1)).setTrialUsed(id);
    verify(stripeService, times(1)).createTrialForPriceId(priceId, "cus_already_exists");

    // Ensure we never tried to create a new Stripe customer
    verify(stripeService, never()).getOrCreateCustomer(anyString());
  }

  @Test
  void testCreateTrial_Success_CreatesStripeCustomerIfMissing() {
    // Arrange
    String priceId = "price_premium";

    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = CREATOR_ID;

    Subscription initialSubscription = new Subscription();
    initialSubscription.id = id;
    initialSubscription.usedTrial = false;
    initialSubscription.subscriptionStatus = SubscriptionStatus.FREE;
    initialSubscription.stripeCustomerId = null; // No stripe customer yet

    Subscription updatedSubscription = new Subscription();
    updatedSubscription.id = id;
    updatedSubscription.usedTrial = false;
    updatedSubscription.subscriptionStatus = SubscriptionStatus.FREE;
    updatedSubscription.stripeCustomerId = "cus_newly_created";

    com.stripe.model.Customer mockCustomer = mock(com.stripe.model.Customer.class);
    when(mockCustomer.getId()).thenReturn("cus_newly_created");

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(initialSubscription);
    when(stripeService.getOrCreateCustomer("user-123")).thenReturn(mockCustomer);
    when(subscriptionService.updateCustomerId(initialSubscription, "cus_newly_created"))
        .thenReturn(updatedSubscription);

    // Act
    paymentService.createTrial(priceId);

    // Assert
    assertTrue(updatedSubscription.usedTrial);
    verify(subscriptionService, times(1)).setTrialUsed(id);
    verify(stripeService, times(1)).createTrialForPriceId(priceId, "cus_newly_created");
  }

  @Test
  void testCreateTrial_ThrowsException_IfTrialAlreadyUsed() {
    // Arrange
    String priceId = "price_premium";

    Subscription mockSubscription = new Subscription();
    mockSubscription.usedTrial = true; // Trial already used!
    mockSubscription.subscriptionStatus = SubscriptionStatus.FREE;
    mockSubscription.stripeCustomerId = "123";

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(mockSubscription);

    // Act & Assert
    CannotUpgradeSubscriptionException exception =
        assertThrows(
            CannotUpgradeSubscriptionException.class, () -> paymentService.createTrial(priceId));

    assertEquals("You already used your free trial", exception.getMessage());

    // Verify downstream executions were blocked
    verify(subscriptionService, never()).setTrialUsed(any(CreatorIdKey.class));
    verify(stripeService, never()).createTrialForPriceId(anyString(), anyString());
  }

  @Test
  void testCreateTrial_ThrowsException_IfAlreadySubscribed() {
    // Arrange
    String priceId = "price_premium";

    Subscription mockSubscription = new Subscription();
    mockSubscription.usedTrial = false;
    mockSubscription.subscriptionStatus = SubscriptionStatus.PREMIUM; // Not FREE!

    when(subscriptionService.getSubscriptionForCurrentUser()).thenReturn(mockSubscription);

    // Act & Assert
    CannotUpgradeSubscriptionException exception =
        assertThrows(
            CannotUpgradeSubscriptionException.class, () -> paymentService.createTrial(priceId));

    assertEquals("You already have a subscription", exception.getMessage());

    // Verify downstream executions were blocked
    verify(stripeService, never()).createTrialForPriceId(anyString(), anyString());
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
