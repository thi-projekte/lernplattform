/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.subscription.rest;

import de.thi.mynd.subscription.service.StripeWebhookService;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/stripe-webhook")
public final class StripeWebhookResource {

  @Inject StripeWebhookService webhookService;

  @POST
  public Response handleWebhook(String payload, @HeaderParam("Stripe-Signature") String sigHeader) {
    webhookService.processWebhook(payload, sigHeader);
    return Response.ok().build();
  }
}
