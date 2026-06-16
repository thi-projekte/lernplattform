/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.subscription.service;

import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public final class PaymentServiceImpl implements PaymentService {

  @Inject SubscriptionService subscriptionService;

  @Inject StripeService stripeService;

  @Inject SecurityIdentity identity;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public StripeSessionDto createInitialSubscriptionSession(String priceId) {

    Subscription subscription = obtainSubscription();

    Session session =
        stripeService.createCheckoutSessionForSubscriptionPrice(
            priceId, subscription.stripeCustomerId);

    Log.infof(
        "Created initial subscription session for customer %s with price %s",
        subscription.stripeCustomerId, priceId);

    return mappingRegistry.map(session, StripeSessionDto.class);
  }

  @Override
  public List<ProductDto> getAllProducts() {
    Subscription subscription = subscriptionService.getSubscriptionForCurrentUser();
    List<Product> products = stripeService.getAllProductsWithPricesAndMetaData();

    Log.tracef("Loading all products from stripe");

    return mappingRegistry.mapList(products, ProductDto.class, subscription.usedTrial);
  }

  @Override
  public void createTrial(String priceId) {
    Subscription subscription = obtainSubscription();

    if (subscription.usedTrial) {
      throw new CannotUpgradeSubscriptionException("You already used your free trial");
    }
    subscription.usedTrial = true;
    subscriptionService.setTrialUsed(subscription.id);

    stripeService.createTrialForPriceId(priceId, subscription.stripeCustomerId);

    Log.infof(
        "Created trial subscription for price %s for user %s",
        priceId, subscription.stripeCustomerId);
  }

  private Subscription obtainSubscription() {
    String creatorId = identity.getPrincipal().getName();

    Subscription subscription = subscriptionService.getSubscriptionForCurrentUser();

    if (subscription.subscriptionStatus != SubscriptionStatus.FREE) {
      throw new CannotUpgradeSubscriptionException("You already have a subscription");
    }

    if (subscription.stripeCustomerId == null) {
      String customerId = stripeService.getOrCreateCustomer(creatorId).getId();
      subscription = subscriptionService.updateCustomerId(subscription, customerId);
    }
    Log.tracef("Obtaining current subscription for user %s", creatorId);
    return subscription;
  }
}
