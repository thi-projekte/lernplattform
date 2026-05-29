package de.thi.mynd.subscription.service;

import com.stripe.model.Customer;
import com.stripe.model.Price;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import io.quarkus.cache.CacheResult;

public interface StripeService {

  @CacheResult(cacheName = "stripe-prices")
  Price obtainPriceForSubscriptionStatus(SubscriptionStatus subscriptionStatus);

  com.stripe.model.checkout.Session createCheckoutSessionForSubscriptionPrice(
      Price price, String customerId);

  com.stripe.model.billingportal.Session createBillingPortalSession(String customerId);

  Customer getOrCreateCustomer(String username);
}
