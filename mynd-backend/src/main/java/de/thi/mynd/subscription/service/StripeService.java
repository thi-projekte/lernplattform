package de.thi.mynd.subscription.service;

import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import io.quarkus.cache.CacheResult;

public interface StripeService {

  @CacheResult(cacheName = "stripe-prices")
  Price obtainPriceForSubscriptionStatus(SubscriptionStatus subscriptionStatus);

  Session createCheckoutSessionForSubscriptionPrice(Price price, String userId);

  void cancelSubscriptionImmediately(String subscriptionId);

  void cancelSubscriptionAtPeriodEnd(String subscriptionId);
}
