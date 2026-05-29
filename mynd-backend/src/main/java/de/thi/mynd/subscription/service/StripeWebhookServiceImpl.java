package de.thi.mynd.subscription.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Product;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.InvalidStripeSignatureException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    switch (event.getType()) {
      case "customer.subscription.updated":
        handleSubscriptionUpdated(event);
        break;
      case "customer.subscription.deleted":
        handleSubscriptionDeleted(event);
        break;
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
      String tier = product.getMetadata().get("tier");
      SubscriptionStatus status = SubscriptionStatus.valueOf(tier);
      subscriptionService.setSubscriptionStatus(subscription.getId(), status);

      Log.infof("Updated subscription info locally coming from webhook");
    }
  }

  private void handleSubscriptionDeleted(Event event) {
    Optional<StripeObject> objectOptional = event.getDataObjectDeserializer().getObject();
    if (objectOptional.isEmpty()) return;

    if (objectOptional.get() instanceof Subscription subscription) {
      subscriptionService.setSubscriptionStatus(subscription.getId(), SubscriptionStatus.FREE);
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

  private Event verifySignatureAndExtractEvent(String payload, String sigHeader) {
    try {
      return Webhook.constructEvent(payload, sigHeader, endpointSecret);
    } catch (SignatureVerificationException e) {
      throw new InvalidStripeSignatureException(e.getMessage());
    }
  }
}
