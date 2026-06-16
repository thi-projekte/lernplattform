/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.StripeFeatureFlagConstants;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.Subscription;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;

@ApplicationScoped
public final class FeatureQuotaRetrievalServiceImpl extends AbstractFeatureQuotaServiceImpl
    implements FeatureQuotaRetrievalService {

  @Override
  public boolean canLearn(String userId) {

    Log.tracef("Checking if user can learn more for user %s", userId);

    FeatureQuota featureQuota =
        findOrUpdateOrCreateDefaultQuota(
            userId, Feature.LearnContentElementOrTopic, LocalDate.now());
    Subscription subscription = subscriptionService.getSubscriptionForUser(userId);

    return featureQuota.count < freeMaxAmountDailyLearning
        || subscription.features.contains(StripeFeatureFlagConstants.UnlimitedLearning);
  }

  @Override
  public boolean canStartNewTopic(String userId) {
    Log.tracef("Checking if user can start more topics for user %s", userId);
    FeatureQuota featureQuota = findOrUpdateOrCreateDefaultQuota(userId, Feature.StartTopic, null);
    Subscription subscription = subscriptionService.getSubscriptionForUser(userId);

    return featureQuota.count < freeMaxAmountParallelTopics
        || subscription.features.contains(StripeFeatureFlagConstants.UnlimitedParallelTopics);
  }
}
