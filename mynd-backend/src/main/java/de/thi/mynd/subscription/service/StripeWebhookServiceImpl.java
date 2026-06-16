/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.InvalidStripeSignatureException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeWebhookServiceImpl implements StripeWebhookService {

  @ConfigProperty(name = "stripe.webhook.secret")
  String endpointSecret;

  @Inject SubscriptionService subscriptionService;
  @Inject StripeService stripeService;

  @Override
  public void processWebhook(String payload, String sigHeader) {
    Event event = verifySignatureAndExtractEvent(payload, sigHeader);

    Log.infof("Received webhook for %s", event.getType());

    switch (event.getType()) {
      case "customer.subscription.created":
        handleSubscriptionCreated(event);
        break;
      case "customer.subscription.updated":
        handleSubscriptionUpdated(event);
        break;
      case "customer.subscription.deleted":
        handleSubscriptionDeleted(event);
        break;
    }
  }

  private void handleSubscriptionCreated(Event event) {
    Optional<StripeObject> objectOptional = event.getDataObjectDeserializer().getObject();
    if (objectOptional.isEmpty()) return;

    if (objectOptional.get() instanceof Subscription subscription) {
      String productId = getProductIdFromSubscription(subscription);
      Product product = stripeService.getFullProductById(productId);
      List<String> features = getFeaturesFromProduct(productId);

      String tier = product.getMetadata().get("tier");
      SubscriptionStatus status = SubscriptionStatus.valueOf(tier);
      subscriptionService.setSubscriptionIdAndStatusForCustomerId(
          subscription.getCustomer(), subscription.getId(), status, features);

      Log.infof("Created subscription info locally coming from webhook");
    }
  }

  private void handleSubscriptionUpdated(Event event) {
    Optional<StripeObject> objectOptional = event.getDataObjectDeserializer().getObject();
    if (objectOptional.isEmpty()) return;

    if (objectOptional.get() instanceof Subscription subscription) {
      if (subscription.getCancelAtPeriodEnd()) {
        Log.infof(
            "Marked subscription %s for cancellation at the end of period", subscription.getId());
        return;
      }

      String productId = getProductIdFromSubscription(subscription);
      Product product = stripeService.getFullProductById(productId);
      List<String> features = getFeaturesFromProduct(productId);

      String tier = product.getMetadata().get("tier");
      SubscriptionStatus status = SubscriptionStatus.valueOf(tier);
      subscriptionService.setSubscriptionStatusForSubscriptionId(
          subscription.getId(), status, features);

      Log.infof("Updated subscription info locally coming from webhook");
    }
  }

  private void handleSubscriptionDeleted(Event event) {
    Optional<StripeObject> objectOptional = event.getDataObjectDeserializer().getObject();
    if (objectOptional.isEmpty()) return;

    if (objectOptional.get() instanceof Subscription subscription) {
      subscriptionService.setSubscriptionStatusForSubscriptionId(
          subscription.getId(), SubscriptionStatus.FREE, new ArrayList<>());
      Log.infof("Updated subscription info locally coming from webhook");
    }
  }

  private String getProductIdFromSubscription(Subscription subscription) {
    var items = subscription.getItems().getData();
    if (items.isEmpty()) {
      throw new ProductNotFoundException("No product listed for this subscription");
    }

    return items.get(0).getPrice().getProduct();
  }

  private List<String> getFeaturesFromProduct(String productId) {
    List<ProductFeature> productFeatures = stripeService.getProductFeatures(productId);
    List<String> features = new ArrayList<>();
    for (ProductFeature productFeature : productFeatures) {
      features.add(productFeature.getEntitlementFeature().getLookupKey());
    }

    return features;
  }

  private Event verifySignatureAndExtractEvent(String payload, String sigHeader) {
    try {
      return Webhook.constructEvent(payload, sigHeader, endpointSecret);
    } catch (SignatureVerificationException e) {
      Log.error("Invalid stripe signature");
      throw new InvalidStripeSignatureException(e.getMessage());
    }
  }
}
