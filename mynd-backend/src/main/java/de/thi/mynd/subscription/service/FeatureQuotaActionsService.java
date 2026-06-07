package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.exception.FeatureQuotaHitException;

public interface FeatureQuotaActionsService {

  void learnContentElement(String userId) throws FeatureQuotaHitException;

  void completeTopic(String userId) throws FeatureQuotaHitException;

  void startNewTopic(String userId) throws FeatureQuotaHitException;
}
