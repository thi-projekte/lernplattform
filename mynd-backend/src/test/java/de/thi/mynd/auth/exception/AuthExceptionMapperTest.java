/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.exception;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * {@link AuthExceptionMapper} is a plain {@code @ServerExceptionMapper} holder, not a CDI bean, so
 * it is instantiated directly rather than injected.
 */
@QuarkusTest
class AuthExceptionMapperTest {

  private final AuthExceptionMapper authExceptionMapper = new AuthExceptionMapper();

  @Test
  void mapProfilePictureNotFoundException_returnsNotFound() {
    Response response =
        authExceptionMapper.mapProfilePictureNotFoundException(
            new ProfilePictureNotFoundException("no picture"));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    assertEquals("no picture", response.getEntity());
  }

  @Test
  void mapNoInvitationsLeftException_returnsBadRequest() {
    Response response =
        authExceptionMapper.mapNoInvitationsLeftException(
            new NoInvitationsLeftException("no invitations left"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("no invitations left", response.getEntity());
  }

  @Test
  void mapCannotAcceptInvitationException_returnsBadRequest() {
    Response response =
        authExceptionMapper.mapCannotAcceptInvitationException(
            new CannotAcceptInvitationException("cannot accept"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("cannot accept", response.getEntity());
  }
}
