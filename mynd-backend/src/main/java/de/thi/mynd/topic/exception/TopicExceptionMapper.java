/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public final class TopicExceptionMapper {

  @ServerExceptionMapper
  public Response mapAssociationAlreadyExistsException(AssociationAlreadyExistsException e) {
    return Response.status(Response.Status.FOUND).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapImportException(ImportException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapCategoryAlreadyExistsException(CategoryAlreadyExistsException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapCategoryNotFoundException(CategoryNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
  }

  @ServerExceptionMapper
  public Response mapCategoryMoveException(CategoryMoveException e) {
    return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
  }
}
