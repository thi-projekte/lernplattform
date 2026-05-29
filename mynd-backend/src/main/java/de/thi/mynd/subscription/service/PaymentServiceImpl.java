package de.thi.mynd.subscription.service;

import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
import de.thi.mynd.subscription.exception.StripeCustomerAlreadyExists;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class PaymentServiceImpl implements PaymentService {

  @Inject SubscriptionService subscriptionService;

  @Inject StripeService stripeService;

  @Inject SecurityIdentity identity;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public StripeSessionDto createInitialSubscriptionSession(
      SubscriptionStatus subscriptionStatus) {
    String creatorId = identity.getPrincipal().getName();

    Subscription subscription = subscriptionService.getSubscriptionForCurrentUser();

    if (subscription.subscriptionStatus != SubscriptionStatus.FREE) {
      throw new CannotUpgradeSubscriptionException("You already have a subscription");
    }

    if (subscription.stripeCustomerId == null) {
      String customerId = stripeService.createCustomer(creatorId).getId();
      subscription =
              subscriptionService.updateCustomerId(
                      subscription, customerId);
    }

    Price price = stripeService.obtainPriceForSubscriptionStatus(subscriptionStatus);
    Session session =
        stripeService.createCheckoutSessionForSubscriptionPrice(
            price, subscription.stripeCustomerId);

    return mappingRegistry.map(session, StripeSessionDto.class);
  }
}
