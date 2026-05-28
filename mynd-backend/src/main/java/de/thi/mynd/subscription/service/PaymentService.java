package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.PaymentSessionDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

public interface PaymentService {

  PaymentSessionDto getCheckoutSessionForSubscription(SubscriptionStatus subscriptionStatus);
}
