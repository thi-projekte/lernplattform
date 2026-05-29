package de.thi.mynd.subscription.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import de.thi.mynd.subscription.exception.InvalidStripeSignatureException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeWebhookServiceImpl implements StripeWebhookService {

    @ConfigProperty(name = "stripe.webhook.secret")
    String endpointSecret;

    @Override
    public void processWebhook(String payload, String sigHeader) {
        Event event = verifySignatureAndExtractEvent(payload, sigHeader);

        switch (event.getType()) {
            case "checkout.session.completed":
                break;
            case "customer.subscription.deleted":
                break;
        }
    }

    private void checkoutSessionCompleted(Event event) {

    }

    private Event verifySignatureAndExtractEvent(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            throw new InvalidStripeSignatureException(e.getMessage());
        }
    }
}
