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
import java.util.Set;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicVoterTest {

  @Inject TopicVoter voter;

  private SecurityIdentity identityFor(String username, String... roles) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(username);
    SecurityIdentity identity = mock(SecurityIdentity.class);
    when(identity.getPrincipal()).thenReturn(principal);
    when(identity.getRoles()).thenReturn(Set.of(roles));
    return identity;
  }

  private Topic newTopic(String creatorId) {
    Topic topic = new Topic();
    topic.creatorId = creatorId;
    return topic;
  }

  @Test
  void supports_topicWithKnownAttribute_returnsTrue() {
    assertTrue(voter.supports(TopicVoter.Create, new Topic()));
    assertTrue(voter.supports(TopicVoter.Update, new Topic()));
    assertTrue(voter.supports(TopicVoter.Delete, new Topic()));
    assertTrue(voter.supports(TopicVoter.AssignForeignTopics, new Topic()));
  }

  @Test
  void supports_topicWithUnsupportedAttribute_returnsFalse() {
    assertFalse(voter.supports("UNKNOWN", new Topic()));
  }

  @Test
  void supports_nonTopicSubject_returnsFalse() {
    assertFalse(voter.supports(TopicVoter.Create, new IndexCard()));
  }

  @Test
  void vote_createWithBuilderRole_returnsTrue() {
    boolean result = voter.vote(identityFor("someone", "builder"), TopicVoter.Create, new Topic());

    assertTrue(result);
  }

  @Test
  void vote_createWithoutBuilderRole_returnsFalse() {
    boolean result = voter.vote(identityFor("someone"), TopicVoter.Create, new Topic());

    assertFalse(result);
  }

  @Test
  void vote_updateOwnTopic_returnsTrue() {
    Topic topic = newTopic("owner");

    assertTrue(voter.vote(identityFor("owner"), TopicVoter.Update, topic));
  }

  @Test
  void vote_updateOthersTopic_returnsFalse() {
    Topic topic = newTopic("owner");

    assertFalse(voter.vote(identityFor("someone-else"), TopicVoter.Update, topic));
  }

  @Test
  void vote_deleteOwnTopic_returnsTrue() {
    Topic topic = newTopic("owner");

    assertTrue(voter.vote(identityFor("owner"), TopicVoter.Delete, topic));
  }

  @Test
  void vote_assignForeignTopicsOnOwnTopic_returnsTrue() {
    Topic topic = newTopic("owner");

    assertTrue(voter.vote(identityFor("owner"), TopicVoter.AssignForeignTopics, topic));
  }

  @Test
  void vote_unknownAttribute_returnsFalse() {
    Topic topic = newTopic("owner");

    assertFalse(voter.vote(identityFor("owner"), "UNKNOWN", topic));
  }
}
