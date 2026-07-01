/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.security;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.topic.entity.Topic;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SecurityServiceTest {

  @Inject SecurityService securityService;

  @InjectMock SecurityIdentity securityIdentity;

  @Test
  void isGranted_noVoterSupportsSubjectAndAttribute_returnsFalse() {
    // No registered Voter subclass supports a plain Object subject, so the loop must exhaust
    // without finding a match and fall through to the default false.
    assertFalse(securityService.isGranted(new Object(), "UNKNOWN"));
  }

  @Test
  void denyUnlessGranted_whenNotGranted_throwsForbiddenException() {
    assertThrows(
        ForbiddenException.class, () -> securityService.denyUnlessGranted(new Object(), "UNKNOWN"));
  }

  @Test
  void denyUnlessGranted_whenUnsupportedAttributeOnKnownEntity_throwsForbiddenException() {
    // TopicVoter.supports() returns false for an attribute it doesn't recognize, so isGranted
    // still falls through to false rather than voting.
    assertThrows(
        ForbiddenException.class, () -> securityService.denyUnlessGranted(new Topic(), "UNKNOWN"));
  }
}
