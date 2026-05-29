package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

public interface SubscriptionService {

  Subscription getSubscriptionForCurrentUser();

  SubscriptionDto getSubscriptionForCurrentUserAsDto();

  Subscription createDefaultSubscriptionForCurrentUser();

  Subscription updateCustomerId(Subscription subscription, String customerId);

  StripeSessionDto createBillingPortalSession();

  void setSubscriptionStatus(String stripeSubscriptionId, SubscriptionStatus status);
}
