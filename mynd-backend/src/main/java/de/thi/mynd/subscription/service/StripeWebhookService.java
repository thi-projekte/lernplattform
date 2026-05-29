package de.thi.mynd.subscription.service;

public interface StripeWebhookService {

    void processWebhook(String payload, String sigHeader);
}
