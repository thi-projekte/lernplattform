package de.thi.mynd.common.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class GlobalExceptionMapper {

  @ServerExceptionMapper
  public Response mapFileTooLargeException(FileTooLargeException e) {
    return Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapInvalidFileTypeException(InvalidFileTypeException e) {
    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapNoFileProvidedException(NoFileProvidedException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapUserNotFoundException(UserNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
  }
}
