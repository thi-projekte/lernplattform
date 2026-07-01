/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.thi.mynd.topic.entity.RtfElement;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContentElementVoterTest {

  @Inject ContentElementVoter voter;

  private SecurityIdentity identityFor(String username) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(username);
    SecurityIdentity identity = mock(SecurityIdentity.class);
    when(identity.getPrincipal()).thenReturn(principal);
    return identity;
  }

  private RtfElement newContentElement(String creatorId) {
    RtfElement element = new RtfElement();
    element.creatorId = creatorId;
    return element;
  }

  @Test
  void supports_contentElementWithAssignAttribute_returnsTrue() {
    assertTrue(voter.supports(ContentElementVoter.Assign, new RtfElement()));
  }

  @Test
  void supports_contentElementWithDeleteAttribute_returnsTrue() {
    assertTrue(voter.supports(ContentElementVoter.Delete, new RtfElement()));
  }

  @Test
  void supports_contentElementWithUnsupportedAttribute_returnsFalse() {
    assertFalse(voter.supports("UNKNOWN", new RtfElement()));
  }

  @Test
  void supports_nonContentElementSubject_returnsFalse() {
    assertFalse(voter.supports(ContentElementVoter.Assign, new Topic()));
  }

  @Test
  void vote_assignOwnContentElement_returnsTrue() {
    RtfElement element = newContentElement("owner");

    assertTrue(voter.vote(identityFor("owner"), ContentElementVoter.Assign, element));
  }

  @Test
  void vote_assignOthersContentElement_returnsFalse() {
    RtfElement element = newContentElement("owner");

    assertFalse(voter.vote(identityFor("someone-else"), ContentElementVoter.Assign, element));
  }

  @Test
  void vote_deleteOwnContentElement_returnsTrue() {
    RtfElement element = newContentElement("owner");

    assertTrue(voter.vote(identityFor("owner"), ContentElementVoter.Delete, element));
  }

  @Test
  void vote_unknownAttribute_returnsFalse() {
    RtfElement element = newContentElement("owner");

    assertFalse(voter.vote(identityFor("owner"), "UNKNOWN", element));
  }
}
