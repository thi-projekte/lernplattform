package de.thi.mynd.progressTracking.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public final class ProgressTrackingExceptionMapper {

  @ServerExceptionMapper
  public Response mapTopicLearnProgressNotStarted(TopicLearnProgressNotStartedException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapTopicLearnProgressAlreadyStarted(TopicLearnProgressAlreadyStartedException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapContentElementLearnProgressAlreadyCompletedException(
      ContentElementLearnProgressAlreadyCompletedException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }
}
