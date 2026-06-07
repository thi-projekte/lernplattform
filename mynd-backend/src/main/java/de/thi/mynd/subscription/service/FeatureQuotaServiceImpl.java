package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.StripeFeatureFlagConstants;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.exception.FeatureQuotaHitException;
import de.thi.mynd.subscription.repository.FeatureQuotaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public final class FeatureQuotaServiceImpl implements FeatureQuotaService {

    @ConfigProperty(name = "mynd.subsciptionFeatures.freeParallelTopicLimit")
    int freeMaxAmountParallelTopics;

    @ConfigProperty(name = "mynd.subsciptionFeatures.freeDailyLearnLimit")
    int freeMaxAmountDailyLearning;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    FeatureQuotaRepository featureQuotaRepository;

    @Override
    @Transactional
    public void learnContentElement(String userId) throws FeatureQuotaHitException {
        FeatureQuota featureQuota = findOrUpdateOrCreateDefaultQuota(userId, Feature.LearnContentElementOrTopic, null);
        featureQuota.count++;

        Subscription subscription = subscriptionService.getSubscriptionForUser(userId);

        if (featureQuota.count > freeMaxAmountDailyLearning && !subscription.features.contains(StripeFeatureFlagConstants.UnlimitedLearning)) {
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

        if (featureQuota.count > freeMaxAmountParallelTopics && !subscription.features.contains(StripeFeatureFlagConstants.UnlimitedParallelTopics)) {
            throw new FeatureQuotaHitException("You cannot have that many topics running in parallel");
        }
        featureQuotaRepository.persistAndFlush(featureQuota);
    }

    private FeatureQuota findOrUpdateOrCreateDefaultQuota(String userId, Feature feature, LocalDate date) {
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
