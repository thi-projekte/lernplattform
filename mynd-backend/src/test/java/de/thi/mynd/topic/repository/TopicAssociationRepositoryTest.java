/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link TopicAssociationRepository} against a real Postgres instance (Quarkus dev
 * services). {@link TopicAssociation#owningTopic} and {@link TopicAssociation#foreignTopic} are
 * persisted via cascade, but {@link Topic#title} is DB-unique, so every fixture topic title is
 * randomized.
 */
@QuarkusTest
class TopicAssociationRepositoryTest {

  @Inject TopicAssociationRepository topicAssociationRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private Topic newTopic(String title) {
    Topic t = new Topic();
    t.title = title;
    t.teaser = "Teaser for " + title;
    t.creatorId = "tester";
    return t;
  }

  private TopicAssociation newAssociation(Topic owner, Topic foreign, String creatorId) {
    TopicAssociation association = new TopicAssociation();
    association.owningTopic = owner;
    association.foreignTopic = foreign;
    association.creatorId = creatorId;
    return association;
  }

  @Test
  @TestTransaction
  void findOwningAssociationsByIdsAndUsername_matchingOwnerAndCreator_returnsAssociation() {
    String creatorId = unique("creator");
    Topic owner = newTopic(unique("Owner"));
    Topic foreign = newTopic(unique("Foreign"));
    TopicAssociation association = newAssociation(owner, foreign, creatorId);
    topicAssociationRepository.persistAndFlush(association);

    List<TopicAssociation> result =
        topicAssociationRepository.findOwningAssociationsByIdsAndUsername(
            owner.id, List.of(foreign.id), creatorId);

    assertEquals(1, result.size());
    assertEquals(association.id, result.get(0).id);
  }

  @Test
  @TestTransaction
  void findOwningAssociationsByIdsAndUsername_wrongUsername_returnsEmpty() {
    String creatorId = unique("creator");
    Topic owner = newTopic(unique("Owner"));
    Topic foreign = newTopic(unique("Foreign"));
    TopicAssociation association = newAssociation(owner, foreign, creatorId);
    topicAssociationRepository.persistAndFlush(association);

    List<TopicAssociation> result =
        topicAssociationRepository.findOwningAssociationsByIdsAndUsername(
            owner.id, List.of(foreign.id), unique("other-creator"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findOwningAssociationsByIdsAndUsername_foreignIdNotInList_returnsEmpty() {
    String creatorId = unique("creator");
    Topic owner = newTopic(unique("Owner"));
    Topic foreign = newTopic(unique("Foreign"));
    TopicAssociation association = newAssociation(owner, foreign, creatorId);
    topicAssociationRepository.persistAndFlush(association);

    List<TopicAssociation> result =
        topicAssociationRepository.findOwningAssociationsByIdsAndUsername(
            owner.id, List.of(UUID.randomUUID()), creatorId);

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void associationExists_ownerToForeignDirection_returnsTrue() {
    Topic owner = newTopic(unique("Owner"));
    Topic foreign = newTopic(unique("Foreign"));
    topicAssociationRepository.persistAndFlush(newAssociation(owner, foreign, "tester"));

    assertTrue(topicAssociationRepository.associationExists(owner, foreign));
  }

  @Test
  @TestTransaction
  void associationExists_reverseDirectionOfSamePair_returnsTrue() {
    Topic owner = newTopic(unique("Owner"));
    Topic foreign = newTopic(unique("Foreign"));
    topicAssociationRepository.persistAndFlush(newAssociation(owner, foreign, "tester"));

    // associationExists is symmetric (OR condition checks both directions of the pair).
    assertTrue(topicAssociationRepository.associationExists(foreign, owner));
  }

  @Test
  @TestTransaction
  void associationExists_nonExistentPair_returnsFalse() {
    Topic a = newTopic(unique("A"));
    Topic b = newTopic(unique("B"));
    // Persist the topics independently (no association between them).
    topicAssociationRepository.getEntityManager().persist(a);
    topicAssociationRepository.getEntityManager().persist(b);
    topicAssociationRepository.getEntityManager().flush();

    assertFalse(topicAssociationRepository.associationExists(a, b));
  }
}
