package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

public interface SubscriptionService {

  boolean canUserUpgradeTo(SubscriptionStatus subscriptionStatus);

  Subscription getSubscriptionForCurrentUser();

  SubscriptionDto getSubscriptionForCurrentUserAsDto();

  Subscription createDefaultSubscriptionForCurrentUser();

  Subscription updateCustomerId(Subscription subscription, String customerId);
}
