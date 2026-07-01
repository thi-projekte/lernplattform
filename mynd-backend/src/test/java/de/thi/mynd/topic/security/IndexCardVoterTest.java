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

import de.thi.mynd.topic.entity.IndexCard;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IndexCardVoterTest {

  @Inject IndexCardVoter voter;

  private SecurityIdentity identityFor(String username) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(username);
    SecurityIdentity identity = mock(SecurityIdentity.class);
    when(identity.getPrincipal()).thenReturn(principal);
    return identity;
  }

  private IndexCard newIndexCard(String creatorId) {
    IndexCard card = new IndexCard();
    card.creatorId = creatorId;
    return card;
  }

  @Test
  void supports_indexCardWithAssignAttribute_returnsTrue() {
    assertTrue(voter.supports(IndexCardVoter.Assign, new IndexCard()));
  }

  @Test
  void supports_indexCardWithDeleteAttribute_returnsTrue() {
    assertTrue(voter.supports(IndexCardVoter.Delete, new IndexCard()));
  }

  @Test
  void supports_indexCardWithUnsupportedAttribute_returnsFalse() {
    assertFalse(voter.supports("UNKNOWN", new IndexCard()));
  }

  @Test
  void supports_nonIndexCardSubject_returnsFalse() {
    assertFalse(voter.supports(IndexCardVoter.Assign, new Topic()));
  }

  @Test
  void vote_assignOwnIndexCard_returnsTrue() {
    IndexCard card = newIndexCard("owner");

    assertTrue(voter.vote(identityFor("owner"), IndexCardVoter.Assign, card));
  }

  @Test
  void vote_assignOthersIndexCard_returnsFalse() {
    IndexCard card = newIndexCard("owner");

    assertFalse(voter.vote(identityFor("someone-else"), IndexCardVoter.Assign, card));
  }

  @Test
  void vote_deleteOwnIndexCard_returnsTrue() {
    IndexCard card = newIndexCard("owner");

    assertTrue(voter.vote(identityFor("owner"), IndexCardVoter.Delete, card));
  }

  @Test
  void vote_deleteOthersIndexCard_returnsFalse() {
    IndexCard card = newIndexCard("owner");

    assertFalse(voter.vote(identityFor("someone-else"), IndexCardVoter.Delete, card));
  }

  @Test
  void vote_unknownAttribute_returnsFalse() {
    IndexCard card = newIndexCard("owner");

    assertFalse(voter.vote(identityFor("owner"), "UNKNOWN", card));
  }
}
