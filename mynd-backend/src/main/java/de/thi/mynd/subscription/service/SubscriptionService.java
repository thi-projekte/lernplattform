package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

import java.util.List;

public interface SubscriptionService {

  Subscription getSubscriptionForCurrentUser();

  SubscriptionDto getSubscriptionForCurrentUserAsDto();

  Subscription createDefaultSubscriptionForCurrentUser();

  Subscription updateCustomerId(Subscription subscription, String customerId);

  StripeSessionDto createBillingPortalSession();

  void setSubscriptionStatusForSubscriptionId(
          String stripeSubscriptionId, SubscriptionStatus status, List<String> features);

  void setSubscriptionIdAndStatusForCustomerId(
      String customerId, String subscriptionId, SubscriptionStatus subscriptionStatus, List<String> features);

  void setTrialUsed(CreatorIdKey id);
}
