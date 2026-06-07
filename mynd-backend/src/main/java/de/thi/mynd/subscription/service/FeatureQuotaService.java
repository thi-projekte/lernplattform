package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.exception.FeatureQuotaHitException;

public interface FeatureQuotaService {

    void learnContentElementOrWholeTopic(String userId) throws FeatureQuotaHitException;

    void startNewTopic(String userId) throws FeatureQuotaHitException;
}
