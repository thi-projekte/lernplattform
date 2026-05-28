package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

public interface SubscriptionService {

    boolean canUserUpgradeTo(SubscriptionStatus subscriptionStatus);

    Subscription getSubscriptionForCurrentUser();

    Subscription createDefaultSubscriptionForCurrentUser();
}
