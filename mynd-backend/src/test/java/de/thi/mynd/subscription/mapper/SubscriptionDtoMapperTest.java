/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.service.FeatureQuotaRetrievalService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SubscriptionDtoMapperTest {

  @Inject SubscriptionDtoMapper subscriptionDtoMapper;

  @InjectMock FeatureQuotaRetrievalService featureQuotaRetrievalService;

  private Subscription subscription(String creatorId) {
    Subscription subscription = new Subscription();
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = creatorId;
    subscription.id = id;
    subscription.creatorId = creatorId;
    subscription.subscriptionStatus = SubscriptionStatus.FREE;
    return subscription;
  }

  @Test
  void mapAndEnrich_stripeCustomerIdPresent_canAccessBillingPortalIsTrue() {
    Subscription subscription = subscription("alice");
    subscription.stripeCustomerId = "cus_123";
    when(featureQuotaRetrievalService.canLearn("alice")).thenReturn(true);
    when(featureQuotaRetrievalService.canStartNewTopic("alice")).thenReturn(true);

    SubscriptionDto dto = subscriptionDtoMapper.mapAndEnrich(subscription);

    assertTrue(dto.canAccessBillingPortal);
  }

  @Test
  void mapAndEnrich_stripeCustomerIdNull_canAccessBillingPortalIsFalse() {
    Subscription subscription = subscription("alice");
    subscription.stripeCustomerId = null;
    when(featureQuotaRetrievalService.canLearn("alice")).thenReturn(true);
    when(featureQuotaRetrievalService.canStartNewTopic("alice")).thenReturn(true);

    SubscriptionDto dto = subscriptionDtoMapper.mapAndEnrich(subscription);

    assertFalse(dto.canAccessBillingPortal);
  }

  @Test
  void mapAndEnrich_copiesCreatorIdAndSubscriptionStatus() {
    Subscription subscription = subscription("bob");
    subscription.subscriptionStatus = SubscriptionStatus.PREMIUM;
    when(featureQuotaRetrievalService.canLearn("bob")).thenReturn(true);
    when(featureQuotaRetrievalService.canStartNewTopic("bob")).thenReturn(true);

    SubscriptionDto dto = subscriptionDtoMapper.mapAndEnrich(subscription);

    assertEquals("bob", dto.creatorId);
    assertEquals(SubscriptionStatus.PREMIUM, dto.subscriptionStatus);
  }

  @Test
  void mapAndEnrich_delegatesCanLearnTopicsToFeatureQuotaService_true() {
    Subscription subscription = subscription("carol");
    when(featureQuotaRetrievalService.canLearn("carol")).thenReturn(true);
    when(featureQuotaRetrievalService.canStartNewTopic("carol")).thenReturn(false);

    SubscriptionDto dto = subscriptionDtoMapper.mapAndEnrich(subscription);

    assertTrue(dto.canLearnTopics);
    assertFalse(dto.canStartNewTopics);
  }

  @Test
  void mapAndEnrich_delegatesCanLearnTopicsToFeatureQuotaService_false() {
    Subscription subscription = subscription("dave");
    when(featureQuotaRetrievalService.canLearn("dave")).thenReturn(false);
    when(featureQuotaRetrievalService.canStartNewTopic("dave")).thenReturn(true);

    SubscriptionDto dto = subscriptionDtoMapper.mapAndEnrich(subscription);

    assertFalse(dto.canLearnTopics);
    assertTrue(dto.canStartNewTopics);
  }

  @Test
  void getEntityType_returnsSubscription() {
    assertEquals(Subscription.class, subscriptionDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsSubscriptionDto() {
    assertEquals(SubscriptionDto.class, subscriptionDtoMapper.getDtoType());
  }
}
