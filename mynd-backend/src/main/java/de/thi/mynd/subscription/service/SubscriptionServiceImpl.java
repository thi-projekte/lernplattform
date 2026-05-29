package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.subscription.dto.SubscriptionDto;
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

    @Inject
    MappingRegistry mappingRegistry;

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
}
