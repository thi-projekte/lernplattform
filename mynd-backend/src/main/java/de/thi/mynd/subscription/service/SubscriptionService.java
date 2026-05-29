package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;

public interface SubscriptionService {

  Subscription getSubscriptionForCurrentUser();

  SubscriptionDto getSubscriptionForCurrentUserAsDto();

  Subscription createDefaultSubscriptionForCurrentUser();

  Subscription updateCustomerId(Subscription subscription, String customerId);

  StripeSessionDto createBillingPortalSession();
}
