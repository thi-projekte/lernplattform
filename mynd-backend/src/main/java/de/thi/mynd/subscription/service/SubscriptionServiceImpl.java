package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.repository.SubscriptionRepository;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public final class SubscriptionServiceImpl implements SubscriptionService {

  @Inject SecurityIdentity identity;

  @Inject SubscriptionRepository subscriptionRepository;

  @Override
  public boolean canUserUpgradeTo(SubscriptionStatus subscriptionStatus) {
    Subscription subscription = getSubscriptionForCurrentUser();
    if (subscription.subscriptionStatus == SubscriptionStatus.FREE
        && subscriptionStatus != SubscriptionStatus.FREE) {
      return true;
    }

    return subscription.subscriptionStatus == SubscriptionStatus.PLUS
        && subscriptionStatus == SubscriptionStatus.PRO;
  }

  @Override
  public Subscription getSubscriptionForCurrentUser() {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = identity.getPrincipal().getName();

    return subscriptionRepository
        .findByIdOptional(id)
        .orElseGet(this::createDefaultSubscriptionForCurrentUser);
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
}
