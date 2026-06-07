package de.thi.mynd.subscription.service;

public interface FeatureQuotaRetrievalService {

    boolean canLearn(String userId);

    boolean canStartNewTopic(String userId);
}
