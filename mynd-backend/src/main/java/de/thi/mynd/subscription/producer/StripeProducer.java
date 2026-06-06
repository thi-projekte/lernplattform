package de.thi.mynd.subscription.producer;

import com.stripe.StripeClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeProducer {

  @ConfigProperty(name = "stripe.api.key")
  String apiKey;

  @Produces
  @ApplicationScoped
  public StripeClient stripeClient() {
    return new StripeClient(apiKey);
  }
}
