package de.thi.mynd.topic.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public final class TopicExceptionMapper {

  @ServerExceptionMapper
  public Response mapAssociationAlreadyExistsException(AssociationAlreadyExistsException e) {
    return Response.status(Response.Status.FOUND).entity(e.getMessage()).build();
  }
}
