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

import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicAssociationVoterTest {

  @Inject TopicAssociationVoter voter;

  private SecurityIdentity identityFor(String username) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(username);
    SecurityIdentity identity = mock(SecurityIdentity.class);
    when(identity.getPrincipal()).thenReturn(principal);
    return identity;
  }

  private TopicAssociation newAssociation(String creatorId) {
    TopicAssociation association = new TopicAssociation();
    association.creatorId = creatorId;
    return association;
  }

  @Test
  void supports_topicAssociationWithDeleteAttribute_returnsTrue() {
    assertTrue(voter.supports(TopicAssociationVoter.Delete, new TopicAssociation()));
  }

  @Test
  void supports_topicAssociationWithUnsupportedAttribute_returnsFalse() {
    assertFalse(voter.supports("UNKNOWN", new TopicAssociation()));
  }

  @Test
  void supports_nonTopicAssociationSubject_returnsFalse() {
    assertFalse(voter.supports(TopicAssociationVoter.Delete, new Topic()));
  }

  @Test
  void vote_deleteOwnAssociation_returnsTrue() {
    TopicAssociation association = newAssociation("owner");

    boolean result = voter.vote(identityFor("owner"), TopicAssociationVoter.Delete, association);

    assertTrue(result);
  }

  @Test
  void vote_deleteOthersAssociation_returnsFalse() {
    TopicAssociation association = newAssociation("owner");

    boolean result = voter.vote(identityFor("someone-else"), TopicAssociationVoter.Delete, association);

    assertFalse(result);
  }

  @Test
  void vote_unknownAttribute_returnsFalse() {
    TopicAssociation association = newAssociation("owner");

    boolean result = voter.vote(identityFor("owner"), "UNKNOWN", association);

    assertFalse(result);
  }
}
