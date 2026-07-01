/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.exception;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * {@link TopicExceptionMapper} is a plain {@code @ServerExceptionMapper} holder, not a CDI bean, so
 * it is instantiated directly rather than injected.
 */
@QuarkusTest
class TopicExceptionMapperTest {

  private final TopicExceptionMapper topicExceptionMapper = new TopicExceptionMapper();

  @Test
  void mapAssociationAlreadyExistsException_returnsFound() {
    Response response =
        topicExceptionMapper.mapAssociationAlreadyExistsException(
            new AssociationAlreadyExistsException("already exists"));

    assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatus());
    assertEquals("already exists", response.getEntity());
  }

  @Test
  void mapImportException_returnsBadRequest() {
    Response response = topicExceptionMapper.mapImportException(new ImportException("bad import"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("bad import", response.getEntity());
  }

  @Test
  void mapCategoryAlreadyExistsException_returnsBadRequest() {
    Response response =
        topicExceptionMapper.mapCategoryAlreadyExistsException(
            new CategoryAlreadyExistsException("category exists"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("category exists", response.getEntity());
  }

  @Test
  void mapCategoryNotFoundException_returnsNotFound() {
    Response response =
        topicExceptionMapper.mapCategoryNotFoundException(
            new CategoryNotFoundException("no category"));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    assertEquals("no category", response.getEntity());
  }

  @Test
  void mapCategoryMoveException_returnsConflict() {
    Response response =
        topicExceptionMapper.mapCategoryMoveException(new CategoryMoveException("cannot move"));

    assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    assertEquals("cannot move", response.getEntity());
  }
}
