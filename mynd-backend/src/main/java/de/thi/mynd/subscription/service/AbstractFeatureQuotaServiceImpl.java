/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.FeatureQuotaId;
import de.thi.mynd.subscription.repository.FeatureQuotaRepository;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class AbstractFeatureQuotaServiceImpl {

  @ConfigProperty(name = "mynd.subsciptionFeatures.freeParallelTopicLimit")
  int freeMaxAmountParallelTopics;

  @ConfigProperty(name = "mynd.subsciptionFeatures.freeDailyLearnLimit")
  int freeMaxAmountDailyLearning;

  @Inject SubscriptionService subscriptionService;

  @Inject FeatureQuotaRepository featureQuotaRepository;

  @Transactional
  protected FeatureQuota findOrUpdateOrCreateDefaultQuota(
      String userId, Feature feature, LocalDate date) {
    Optional<FeatureQuota> featureQuotaOptional =
        date != null
            ? featureQuotaRepository.findByCreatorAndFeatureAndDate(userId, feature, date)
            : Optional.empty();
    if (featureQuotaOptional.isPresent()) {
      return featureQuotaOptional.get();
    }

    featureQuotaOptional = featureQuotaRepository.findByCreatorAndFeature(userId, feature);
    if (featureQuotaOptional.isPresent()) {
      FeatureQuota featureQuota = featureQuotaOptional.get();

      if (date != null) {
        featureQuota.dayAccountedFor = date;
        featureQuota.count = 0;
      }

      featureQuotaRepository.persistAndFlush(featureQuota);

      Log.infof("Updated feature quota %s for user %s", feature.name(), userId);

      return featureQuota;
    }

    FeatureQuotaId id = new FeatureQuotaId();
    id.creatorId = userId;
    id.feature = feature;
    FeatureQuota featureQuota = new FeatureQuota();
    featureQuota.id = id;
    featureQuota.dayAccountedFor = date;
    featureQuotaRepository.persistAndFlush(featureQuota);

    Log.infof("Created feature quota %s for user %s", feature.name(), userId);

    return featureQuota;
  }
}
