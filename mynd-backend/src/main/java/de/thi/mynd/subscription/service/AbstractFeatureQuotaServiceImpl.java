package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.repository.FeatureQuotaRepository;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.util.Optional;

public abstract class AbstractFeatureQuotaServiceImpl {

    @ConfigProperty(name = "mynd.subsciptionFeatures.freeParallelTopicLimit")
    int freeMaxAmountParallelTopics;

    @ConfigProperty(name = "mynd.subsciptionFeatures.freeDailyLearnLimit")
    int freeMaxAmountDailyLearning;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    FeatureQuotaRepository featureQuotaRepository;

    protected FeatureQuota findOrUpdateOrCreateDefaultQuota(String userId, Feature feature, LocalDate date) {
        Optional<FeatureQuota> featureQuotaOptional = date != null ? featureQuotaRepository.findByCreatorAndFeatureAndDate(userId, feature, date) : Optional.empty();
        if (featureQuotaOptional.isPresent()) {
            return featureQuotaOptional.get();
        }

        featureQuotaOptional = featureQuotaRepository.findByCreatorAndFeature(userId, feature);
        if (featureQuotaOptional.isPresent()) {
            FeatureQuota featureQuota =  featureQuotaOptional.get();

            if (date != null) {
                featureQuota.dayAccountedFor = date;
                featureQuota.count = 0;
            }

            featureQuotaRepository.persistAndFlush(featureQuota);

            return featureQuota;
        }

        CreatorIdKey id = new CreatorIdKey();
        id.creatorId = userId;
        FeatureQuota featureQuota = new FeatureQuota();
        featureQuota.id = id;
        featureQuota.feature = feature;
        featureQuota.dayAccountedFor = date;
        featureQuotaRepository.persistAndFlush(featureQuota);

        return featureQuota;
    }
}
