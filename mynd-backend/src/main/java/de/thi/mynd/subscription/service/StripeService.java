/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.subscription.service;

import com.stripe.model.*;
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

  @CacheResult(cacheName = "features")
  List<ProductFeature> getProductFeatures(String productId);
}
