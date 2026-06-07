package de.thi.mynd.subscription.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.exception.FeatureQuotaHitException;
import de.thi.mynd.subscription.repository.FeatureQuotaRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public final class FeatureQuotaServiceImpl implements FeatureQuotaService {

    @Inject
    SecurityIdentity identity;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    FeatureQuotaRepository featureQuotaRepository;

    @Override
    @Transactional
    public void learnContentElementOrWholeTopic(String userId) throws FeatureQuotaHitException {

    }

    @Override
    @Transactional
    public void startNewTopic(String userId) throws FeatureQuotaHitException {
        LocalDate today = LocalDate.now();
        FeatureQuota featureQuota = findOrCreateDefaultQuota(userId, Feature.StartTopic, today);
        featureQuota.count++;

        Subscription subscription = subscriptionService.getSubscriptionForUser(userId);

    }

    private FeatureQuota findOrCreateDefaultQuota(String userId, Feature feature, LocalDate date) {
        Optional<FeatureQuota> featureQuotaOptional = featureQuotaRepository.findByCreatorAndFeatureAndDate(userId, feature, date);
        if (featureQuotaOptional.isPresent()) {
            return featureQuotaOptional.get();
        }

        featureQuotaOptional = featureQuotaRepository.findByCreatorAndFeature(userId, feature);
        if (featureQuotaOptional.isPresent()) {
            FeatureQuota featureQuota =  featureQuotaOptional.get();
            featureQuota.dayAccountedFor = date;
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
