package de.thi.mynd.subscription.service;

import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import io.quarkus.cache.CacheResult;
import java.util.List;

public interface StripeService {

  @CacheResult(cacheName = "products")
  List<Product> getAllProductsWithPricesAndMetaData();

  @CacheResult(cacheName = "stripe-product-by-price")
  Product getFullProductById(String productId);

  @CacheResult(cacheName = "prices")
  List<Price> getAllPricesForProduct(String productId);

  com.stripe.model.checkout.Session createCheckoutSessionForSubscriptionPrice(
      String priceId, String customerId);

  com.stripe.model.billingportal.Session createBillingPortalSession(String customerId);

  Customer getOrCreateCustomer(String username);

  Subscription createTrialForPriceId(String priceId, String customerId);
}
