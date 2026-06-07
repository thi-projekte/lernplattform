package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.StripeFeatureFlagConstants;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.exception.FeatureQuotaHitException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;

@ApplicationScoped
public final class FeatureQuotaActionsServiceImpl extends AbstractFeatureQuotaServiceImpl
    implements FeatureQuotaActionsService {

  @Override
  @Transactional
  public void learnContentElement(String userId) throws FeatureQuotaHitException {
    LocalDate today = LocalDate.now();
    FeatureQuota featureQuota =
        findOrUpdateOrCreateDefaultQuota(userId, Feature.LearnContentElementOrTopic, today);
    featureQuota.count++;

    Subscription subscription = subscriptionService.getSubscriptionForUser(userId);

    if (featureQuota.count > freeMaxAmountDailyLearning
        && !subscription.features.contains(StripeFeatureFlagConstants.UnlimitedLearning)) {
      throw new FeatureQuotaHitException("You cannot learn more content elements today");
    }
    featureQuotaRepository.persistAndFlush(featureQuota);
  }

  @Override
  @Transactional
  public void completeTopic(String userId) throws FeatureQuotaHitException {
    FeatureQuota featureQuota = findOrUpdateOrCreateDefaultQuota(userId, Feature.StartTopic, null);
    if (featureQuota.count > 0) {
      featureQuota.count--;
      featureQuotaRepository.persistAndFlush(featureQuota);
    }
  }

  @Override
  @Transactional
  public void startNewTopic(String userId) throws FeatureQuotaHitException {
    FeatureQuota featureQuota = findOrUpdateOrCreateDefaultQuota(userId, Feature.StartTopic, null);
    featureQuota.count++;

    Subscription subscription = subscriptionService.getSubscriptionForUser(userId);

    if (featureQuota.count > freeMaxAmountParallelTopics
        && !subscription.features.contains(StripeFeatureFlagConstants.UnlimitedParallelTopics)) {
      throw new FeatureQuotaHitException("You cannot have that many topics running in parallel");
    }
    featureQuotaRepository.persistAndFlush(featureQuota);
  }
}
