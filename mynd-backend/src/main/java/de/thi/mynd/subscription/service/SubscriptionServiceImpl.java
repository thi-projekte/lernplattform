package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.CannotUpgradeSubscriptionException;
import de.thi.mynd.subscription.exception.SubscriptionNotFoundException;
import de.thi.mynd.subscription.repository.SubscriptionRepository;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public final class SubscriptionServiceImpl implements SubscriptionService {

  @Inject SecurityIdentity identity;

  @Inject SubscriptionRepository subscriptionRepository;

  @Inject MappingRegistry mappingRegistry;
  @Inject StripeService stripeService;

  @Override
  public Subscription getSubscriptionForCurrentUser() {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = identity.getPrincipal().getName();

    return subscriptionRepository
        .findByIdOptional(id)
        .orElseGet(this::createDefaultSubscriptionForCurrentUser);
  }

  @Override
  public SubscriptionDto getSubscriptionForCurrentUserAsDto() {
    return mappingRegistry.map(getSubscriptionForCurrentUser(), SubscriptionDto.class);
  }

  @Override
  @Transactional
  public Subscription createDefaultSubscriptionForCurrentUser() {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = identity.getPrincipal().getName();
    Subscription subscription = new Subscription();
    subscription.id = id;
    subscription.subscriptionStatus = SubscriptionStatus.FREE;

    subscriptionRepository.persistAndFlush(subscription);

    Log.infof("Successfully created default subscription for user %s", id.creatorId);
    return subscription;
  }

  @Transactional
  @Override
  public Subscription updateCustomerId(Subscription subscription, String customerId) {
    Subscription merged = subscriptionRepository.getEntityManager().merge(subscription);
    merged.stripeCustomerId = customerId;
    subscriptionRepository.persistAndFlush(merged);

    return merged;
  }

  @Override
  public StripeSessionDto createBillingPortalSession() {
    Subscription subscription = getSubscriptionForCurrentUser();
    if (subscription.stripeCustomerId == null) {
      throw new CannotUpgradeSubscriptionException(
          "There is no customer registered for this subscription");
    }

    return mappingRegistry.map(
        stripeService.createBillingPortalSession(subscription.stripeCustomerId),
        StripeSessionDto.class);
  }

  @Override
  @Transactional
  public void setSubscriptionStatusForSubscriptionId(
      String stripeSubscriptionId, SubscriptionStatus status, List<String> features) {
    Optional<Subscription> subscriptionOptional =
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    if (subscriptionOptional.isEmpty()) {
      throw new SubscriptionNotFoundException("This subscription does not exist");
    }

    Subscription subscription = subscriptionOptional.get();
    subscription.features = features;
    subscription.subscriptionStatus = status;

    subscriptionRepository.persistAndFlush(subscription);
  }

  @Override
  @Transactional
  public void setSubscriptionIdAndStatusForCustomerId(
      String customerId, String subscriptionId, SubscriptionStatus status, List<String> features) {
    Optional<Subscription> subscriptionOptional =
        subscriptionRepository.findByStripeCustomerId(customerId);
    if (subscriptionOptional.isEmpty()) {
      throw new SubscriptionNotFoundException("This subscription does not exist");
    }

    Subscription subscription = subscriptionOptional.get();
    subscription.features = features;
    subscription.stripeSubscriptionId = subscriptionId;
    subscription.subscriptionStatus = status;

    subscriptionRepository.persistAndFlush(subscription);
  }

  @Override
  @Transactional
  public void setTrialUsed(CreatorIdKey id) {
    Optional<Subscription> subscriptionOptional = subscriptionRepository.findByIdOptional(id);
    if (subscriptionOptional.isEmpty()) {
      throw new SubscriptionNotFoundException("This subscription does not exist");
    }

    Subscription subscription = subscriptionOptional.get();
    subscription.usedTrial = true;

    subscriptionRepository.persistAndFlush(subscription);
  }
}
