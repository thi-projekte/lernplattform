/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
