package de.thi.mynd.subscription.rest;

import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.request.SubscribeRequest;
import de.thi.mynd.subscription.service.PaymentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/payments")
@RolesAllowed("authorizedUser")
@Tag(name = "Payment")
@SecurityRequirement(name = "keycloak")
public final class PaymentResource {

  @Inject PaymentService paymentService;

  @POST
  @Path("/subscribe")
  @Operation(
      summary = "Creates a new checkout session",
      description = "Creates a new checkout session for one of our subscriptions.")
  public StripeSessionDto createSubscriptionSession(
          SubscribeRequest request) {
    return paymentService.createInitialSubscriptionSession(request.subscriptionStatus);
  }
}
