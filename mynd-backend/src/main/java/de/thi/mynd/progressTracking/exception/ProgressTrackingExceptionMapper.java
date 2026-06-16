/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.progressTracking.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public final class ProgressTrackingExceptionMapper {

  @ServerExceptionMapper
  public Response mapTopicLearnProgressNotStarted(TopicLearnProgressNotStartedException e) {
    return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapTopicLearnProgressAlreadyStarted(TopicLearnProgressAlreadyStartedException e) {
    return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapContentElementLearnProgressAlreadyCompletedException(
      ContentElementLearnProgressAlreadyCompletedException e) {
    return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
  }
}
