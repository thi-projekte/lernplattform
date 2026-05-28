package de.thi.mynd.subscription.service;

import com.stripe.model.checkout.Session;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

public interface PaymentService {

    Session getCheckoutSessionForSubscription(SubscriptionStatus subscriptionStatus);
}
