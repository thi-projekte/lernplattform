package de.thi.mynd.subscription.service;

import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.PaymentSessionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
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
  public PaymentSessionDto getCheckoutSessionForSubscription(
      SubscriptionStatus subscriptionStatus) {
    String creatorId = identity.getPrincipal().getName();

    if (!subscriptionService.canUserUpgradeTo(subscriptionStatus)) {
      throw new CannotUpgradeSubscriptionException(
          "You cannot upgrade your subscription to that status");
    }

    Subscription subscription = subscriptionService.getSubscriptionForCurrentUser();

    if (subscription.stripeCustomerId == null) {
      subscription =
          subscriptionService.updateCustomerId(
              subscription, stripeService.createCustomer(creatorId).getId());
    }

    if (subscription.subscriptionStatus != SubscriptionStatus.FREE) {
      stripeService.cancelSubscriptionImmediately(subscription.stripeSubscriptionId);
    }

    Price price = stripeService.obtainPriceForSubscriptionStatus(subscriptionStatus);
    Session session =
        stripeService.createCheckoutSessionForSubscriptionPrice(
            price, subscription.stripeCustomerId);

    return mappingRegistry.map(session, PaymentSessionDto.class);
  }
}
