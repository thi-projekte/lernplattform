/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.exception;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * {@link CommonExceptionMapper} is a plain {@code @ServerExceptionMapper} holder, not a CDI bean,
 * so it is instantiated directly rather than injected.
 */
@QuarkusTest
class CommonExceptionMapperTest {

  private final CommonExceptionMapper commonExceptionMapper = new CommonExceptionMapper();

  @Test
  void mapFileTooLargeException_returnsRequestEntityTooLarge() {
    Response response =
        commonExceptionMapper.mapFileTooLargeException(new FileTooLargeException("too large"));

    assertEquals(Response.Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode(), response.getStatus());
    assertEquals("too large", response.getEntity());
  }

  @Test
  void mapInvalidFileTypeException_returnsUnsupportedMediaType() {
    Response response =
        commonExceptionMapper.mapInvalidFileTypeException(new InvalidFileTypeException("bad type"));

    assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
    assertEquals("bad type", response.getEntity());
  }

  @Test
  void mapNoFileProvidedException_returnsBadRequest() {
    Response response =
        commonExceptionMapper.mapNoFileProvidedException(new NoFileProvidedException("no file"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("no file", response.getEntity());
  }

  @Test
  void mapUserNotFoundException_returnsNotFound() {
    Response response =
        commonExceptionMapper.mapUserNotFoundException(new UserNotFoundException("no user"));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    assertEquals("no user", response.getEntity());
  }

  @Test
  void mapEntityInstanceNotFoundException_returnsNotFound() {
    Response response =
        commonExceptionMapper.mapEntityInstanceNotFoundException(
            new EntityInstanceNotFoundException("no entity"));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    assertEquals("no entity", response.getEntity());
  }
}
