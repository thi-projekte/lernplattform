/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.rest;

import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.request.SubscribeRequest;
import de.thi.mynd.subscription.service.PaymentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/payments")
@Tag(name = "Payment")
@RolesAllowed("authorizedUser")
@SecurityRequirement(name = "keycloak")
public final class PaymentResource {

  @Inject PaymentService paymentService;

  @POST
  @Path("/subscribe")
  @Operation(
      summary = "Creates a new checkout session",
      description = "Creates a new checkout session for one of our subscriptions.")
  public StripeSessionDto createSubscriptionSession(SubscribeRequest request) {
    return paymentService.createInitialSubscriptionSession(request.priceId);
  }

  @GET
  @Path("/products")
  @Operation(
      summary = "Fetches all products from stripe",
      description = "Fetches all products from stripe")
  public List<ProductDto> getProducts() {
    return paymentService.getAllProducts();
  }

  @POST
  @Path("/trial")
  @Operation(
      summary = "Creates a new trial",
      description = "Creates a new trial for the subscription linked to the current price")
  public Response createTrial(SubscribeRequest request) {
    paymentService.createTrial(request.priceId);
    return Response.ok().build();
  }
}
