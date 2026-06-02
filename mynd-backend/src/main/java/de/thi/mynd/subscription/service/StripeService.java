package de.thi.mynd.subscription.service;

import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Product;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import io.quarkus.cache.CacheResult;

import java.util.List;

public interface StripeService {

  List<Product> getAllProductsWithPricesAndMetaData();

  @CacheResult(cacheName = "stripe-product-by-price")
  Product getFullProductById(String productId);

  com.stripe.model.checkout.Session createCheckoutSessionForSubscriptionPrice(
      String priceId, String customerId);

  com.stripe.model.billingportal.Session createBillingPortalSession(String customerId);

  Customer getOrCreateCustomer(String username);
}
