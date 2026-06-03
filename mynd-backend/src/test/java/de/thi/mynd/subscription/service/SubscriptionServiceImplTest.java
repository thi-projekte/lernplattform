package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
import de.thi.mynd.subscription.exception.SubscriptionNotFoundException;
import de.thi.mynd.subscription.repository.SubscriptionRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.security.Principal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SubscriptionServiceImplTest {

  @Inject SubscriptionService subscriptionService;

  @InjectMock SubscriptionRepository subscriptionRepository;

  @InjectMock SecurityIdentity identity;

  @InjectMock MappingRegistry mappingRegistry;

  @InjectMock StripeService stripeService;

  private static final String CREATOR_ID = "user-123";
  private static final String CUSTOMER_ID = "cus_abc123";
  private static final String STRIPE_SUBSCRIPTION_ID = "sub_xyz";

  @BeforeEach
  void setUp() {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(CREATOR_ID);
    when(identity.getPrincipal()).thenReturn(principal);
  }

  // --- getSubscriptionForCurrentUser ---

  @Test
  void getSubscriptionForCurrentUser_whenSubscriptionExists_returnsIt() {
    Subscription existing = subscriptionWithStatus(SubscriptionStatus.PREMIUM);
    when(subscriptionRepository.findByIdOptional(
            argThat(key -> CREATOR_ID.equals(((CreatorIdKey) key).creatorId))))
        .thenReturn(Optional.of(existing));

    Subscription result = subscriptionService.getSubscriptionForCurrentUser();

    assertSame(existing, result);
    verify(subscriptionRepository, never()).persistAndFlush(any());
  }

  @Test
  void getSubscriptionForCurrentUser_whenNoSubscriptionExists_createsDefaultAndReturnsIt() {
    when(subscriptionRepository.findByIdOptional(any())).thenReturn(Optional.empty());

    Subscription result = subscriptionService.getSubscriptionForCurrentUser();

    assertNotNull(result);
    assertEquals(SubscriptionStatus.FREE, result.subscriptionStatus);
    assertEquals(CREATOR_ID, result.id.creatorId);
    verify(subscriptionRepository).persistAndFlush(any(Subscription.class));
  }

  // --- getSubscriptionForCurrentUserAsDto ---

  @Test
  void getSubscriptionForCurrentUserAsDto_mapsSubscriptionToDto() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.FREE);
    SubscriptionDto expectedDto = SubscriptionDto.builder().build();

    when(subscriptionRepository.findByIdOptional(any())).thenReturn(Optional.of(subscription));
    when(mappingRegistry.map(subscription, SubscriptionDto.class)).thenReturn(expectedDto);

    SubscriptionDto result = subscriptionService.getSubscriptionForCurrentUserAsDto();

    assertSame(expectedDto, result);
    verify(mappingRegistry).map(subscription, SubscriptionDto.class);
  }

  // --- createDefaultSubscriptionForCurrentUser ---

  @Test
  void createDefaultSubscriptionForCurrentUser_persistsAndReturnsSubscriptionWithFreeStatus() {
    Subscription result = subscriptionService.createDefaultSubscriptionForCurrentUser();

    assertNotNull(result);
    assertEquals(SubscriptionStatus.FREE, result.subscriptionStatus);
    assertEquals(CREATOR_ID, result.id.creatorId);
    verify(subscriptionRepository).persistAndFlush(result);
  }

  @Test
  void createDefaultSubscriptionForCurrentUser_usesCurrentUserIdentityForCreatorId() {
    subscriptionService.createDefaultSubscriptionForCurrentUser();

    verify(identity, atLeastOnce()).getPrincipal();
  }

  // --- updateCustomerId ---

  @Test
  void updateCustomerId_mergesAndPersistsWithNewCustomerId() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.FREE);
    Subscription merged = subscriptionWithStatus(SubscriptionStatus.FREE);

    EntityManager em = mock(EntityManager.class);
    when(subscriptionRepository.getEntityManager()).thenReturn(em);
    when(em.merge(subscription)).thenReturn(merged);

    Subscription result = subscriptionService.updateCustomerId(subscription, CUSTOMER_ID);

    assertEquals(CUSTOMER_ID, result.stripeCustomerId);
    verify(subscriptionRepository).persistAndFlush(merged);
  }

  @Test
  void updateCustomerId_returnsMergedEntity() {
    Subscription original = subscriptionWithStatus(SubscriptionStatus.FREE);
    Subscription merged = subscriptionWithStatus(SubscriptionStatus.FREE);

    EntityManager em = mock(EntityManager.class);
    when(subscriptionRepository.getEntityManager()).thenReturn(em);
    when(em.merge(original)).thenReturn(merged);

    Subscription result = subscriptionService.updateCustomerId(original, CUSTOMER_ID);

    assertSame(merged, result);
    assertNotSame(original, result);
  }

  // --- createBillingPortalSession ---

  @Test
  void createBillingPortalSession_withExistingCustomer_returnsSessionDto() {
    Subscription subscription = subscriptionWithCustomer();
    com.stripe.model.billingportal.Session billingSession =
        mock(com.stripe.model.billingportal.Session.class);
    StripeSessionDto expectedDto = StripeSessionDto.builder().build();

    when(subscriptionRepository.findByIdOptional(any())).thenReturn(Optional.of(subscription));
    when(stripeService.createBillingPortalSession(CUSTOMER_ID)).thenReturn(billingSession);
    when(mappingRegistry.map(billingSession, StripeSessionDto.class)).thenReturn(expectedDto);

    StripeSessionDto result = subscriptionService.createBillingPortalSession();

    assertSame(expectedDto, result);
    verify(stripeService).createBillingPortalSession(CUSTOMER_ID);
  }

  @Test
  void createBillingPortalSession_withNoCustomer_throwsCannotUpgradeSubscriptionException() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.FREE);
    subscription.stripeCustomerId = null;

    when(subscriptionRepository.findByIdOptional(any())).thenReturn(Optional.of(subscription));

    CannotUpgradeSubscriptionException ex =
        assertThrows(
            CannotUpgradeSubscriptionException.class,
            () -> subscriptionService.createBillingPortalSession());

    assertEquals("There is no customer registered for this subscription", ex.getMessage());
    verifyNoInteractions(stripeService);
  }

  // --- setSubscriptionStatusForSubscriptionId ---

  @Test
  void setSubscriptionStatusForSubscriptionId_withValidId_updatesStatus() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.PREMIUM);
    when(subscriptionRepository.findByStripeSubscriptionId(STRIPE_SUBSCRIPTION_ID))
        .thenReturn(Optional.of(subscription));

    subscriptionService.setSubscriptionStatusForSubscriptionId(
        STRIPE_SUBSCRIPTION_ID, SubscriptionStatus.FREE);

    assertEquals(SubscriptionStatus.FREE, subscription.subscriptionStatus);
    verify(subscriptionRepository).persistAndFlush(subscription);
  }

  @Test
  void setSubscriptionStatusForSubscriptionId_withUnknownId_throwsSubscriptionNotFoundException() {
    when(subscriptionRepository.findByStripeSubscriptionId(STRIPE_SUBSCRIPTION_ID))
        .thenReturn(Optional.empty());

    SubscriptionNotFoundException ex =
        assertThrows(
            SubscriptionNotFoundException.class,
            () ->
                subscriptionService.setSubscriptionStatusForSubscriptionId(
                    STRIPE_SUBSCRIPTION_ID, SubscriptionStatus.FREE));

    assertEquals("This subscription does not exist", ex.getMessage());
    verify(subscriptionRepository, never()).persistAndFlush(any());
  }

  // --- setSubscriptionIdAndStatusForCustomerId ---

  @Test
  void setSubscriptionIdAndStatusForCustomerId_withValidCustomer_updatesSubscriptionIdAndStatus() {
    Subscription subscription = subscriptionWithStatus(SubscriptionStatus.FREE);
    when(subscriptionRepository.findByStripeCustomerId(CUSTOMER_ID))
        .thenReturn(Optional.of(subscription));

    subscriptionService.setSubscriptionIdAndStatusForCustomerId(
        CUSTOMER_ID, STRIPE_SUBSCRIPTION_ID, SubscriptionStatus.PREMIUM);

    assertEquals(STRIPE_SUBSCRIPTION_ID, subscription.stripeSubscriptionId);
    assertEquals(SubscriptionStatus.PREMIUM, subscription.subscriptionStatus);
    verify(subscriptionRepository).persistAndFlush(subscription);
  }

  @Test
  void
      setSubscriptionIdAndStatusForCustomerId_withUnknownCustomer_throwsSubscriptionNotFoundException() {
    when(subscriptionRepository.findByStripeCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

    SubscriptionNotFoundException ex =
        assertThrows(
            SubscriptionNotFoundException.class,
            () ->
                subscriptionService.setSubscriptionIdAndStatusForCustomerId(
                    CUSTOMER_ID, STRIPE_SUBSCRIPTION_ID, SubscriptionStatus.PREMIUM));

    assertEquals("This subscription does not exist", ex.getMessage());
    verify(subscriptionRepository, never()).persistAndFlush(any());
  }

  @Test
  void testSetTrialUsed_Success() {
    // Arrange
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = "creator";

    // Setup a mock entity entity where usedTrial is initially false
    Subscription mockSubscription = new Subscription();
    mockSubscription.id = id;
    mockSubscription.usedTrial = false;

    when(subscriptionRepository.findByIdOptional(id)).thenReturn(Optional.of(mockSubscription));

    // Act
    subscriptionService.setTrialUsed(id);

    // Assert
    assertTrue(mockSubscription.usedTrial, "The usedTrial flag should be set to true");

    // Verify that the repository actually persisted and flushed the updated entity
    verify(subscriptionRepository, times(1)).persistAndFlush(mockSubscription);
  }

  @Test
  void testSetTrialUsed_SubscriptionNotFound_ThrowsException() {
    // Arrange
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = "creator";

    // Simulate repository returning an empty Optional
    when(subscriptionRepository.findByIdOptional(id)).thenReturn(Optional.empty());

    // Act & Assert
    SubscriptionNotFoundException exception =
        assertThrows(
            SubscriptionNotFoundException.class, () -> subscriptionService.setTrialUsed(id));

    assertEquals("This subscription does not exist", exception.getMessage());

    // Verify that persistAndFlush was never called because the method should fail early
    verify(subscriptionRepository, never()).persistAndFlush(any(Subscription.class));
  }

  // --- Helpers ---

  private Subscription subscriptionWithStatus(SubscriptionStatus status) {
    Subscription s = new Subscription();
    CreatorIdKey key = new CreatorIdKey();
    key.creatorId = CREATOR_ID;
    s.id = key;
    s.subscriptionStatus = status;
    return s;
  }

  private Subscription subscriptionWithCustomer() {
    Subscription s = subscriptionWithStatus(SubscriptionStatus.PREMIUM);
    s.stripeCustomerId = SubscriptionServiceImplTest.CUSTOMER_ID;
    return s;
  }
}
