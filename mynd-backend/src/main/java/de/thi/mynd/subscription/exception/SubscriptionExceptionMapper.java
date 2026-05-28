package de.thi.mynd.subscription.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public final class SubscriptionExceptionMapper {

  @ServerExceptionMapper
  public Response mapCannotUpgradeSubscriptionException(CannotUpgradeSubscriptionException e) {
    return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapProductNotFoundException(ProductNotFoundException e) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapHandledStripeException(HandledStripeException e) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
  }
}
