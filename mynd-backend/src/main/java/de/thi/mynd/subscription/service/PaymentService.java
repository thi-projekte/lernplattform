package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

public interface PaymentService {

  StripeSessionDto createInitialSubscriptionSession(SubscriptionStatus subscriptionStatus);
}
