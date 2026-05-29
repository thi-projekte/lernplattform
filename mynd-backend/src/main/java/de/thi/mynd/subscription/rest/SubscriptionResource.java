package de.thi.mynd.subscription.rest;

import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.service.SubscriptionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/subscriptions")
@RolesAllowed("authorizedUser")
@Tag(name = "Subscriptions")
@SecurityRequirement(name = "keycloak")
public final class SubscriptionResource {

    @Inject
    SubscriptionService subscriptionService;

    @GET
    @Operation(
            summary = "Gets the subscription status for the current user",
            description = "Gets the subscription status for the current user")
    public SubscriptionDto getSubscription() {
        return subscriptionService.getSubscriptionForCurrentUserAsDto();
    }
}
