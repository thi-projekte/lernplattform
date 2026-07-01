/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.entity.LearnProgressContentElement;
import de.thi.mynd.progressTracking.entity.LearnProgressContentElementId;
import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link LearnProgressContentElementRepository} against a real Postgres instance (Quarkus
 * dev services). {@link LearnProgressContentElement} carries a foreign key (via the shared
 * creatorId/topicId columns) to {@link LearnProgressTopic}, so a parent topic progress row must be
 * persisted first for every fixture.
 */
@QuarkusTest
class LearnProgressContentElementRepositoryTest {

  @Inject LearnProgressContentElementRepository learnProgressContentElementRepository;
  @Inject LearnProgressTopicRepository learnProgressTopicRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private LearnProgressTopic persistParentTopic(String creatorId, UUID topicId) {
    LearnProgressTopic t = new LearnProgressTopic();
    LearnProgressTopicId id = new LearnProgressTopicId();
    id.creatorId = creatorId;
    id.topicId = topicId;
    t.id = id;
    t.creatorId = creatorId;
    t.status = LearnProgressStatus.STARTED;
    t.contentElementsToComplete = 1;
    learnProgressTopicRepository.persistAndFlush(t);
    return t;
  }

  private LearnProgressContentElement newContentElement(
      String creatorId, UUID topicId, UUID contentElementId) {
    LearnProgressContentElement e = new LearnProgressContentElement();
    LearnProgressContentElementId id = new LearnProgressContentElementId();
    id.creatorId = creatorId;
    id.topicId = topicId;
    id.contentElementId = contentElementId;
    e.id = id;
    e.creatorId = creatorId;
    e.completed = false;
    return e;
  }

  @Test
  @TestTransaction
  void findByContentElementIdAndCreatorId_existingRow_isFound() {
    String creatorId = unique("creator");
    UUID topicId = UUID.randomUUID();
    UUID contentElementId = UUID.randomUUID();
    persistParentTopic(creatorId, topicId);
    learnProgressContentElementRepository.persistAndFlush(
        newContentElement(creatorId, topicId, contentElementId));

    Optional<LearnProgressContentElement> result =
        learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, creatorId);

    assertTrue(result.isPresent());
    assertEquals(contentElementId, result.get().id.contentElementId);
    assertFalse(result.get().completed);
  }

  @Test
  @TestTransaction
  void findByContentElementIdAndCreatorId_wrongCreator_returnsEmpty() {
    String creatorId = unique("creator");
    UUID topicId = UUID.randomUUID();
    UUID contentElementId = UUID.randomUUID();
    persistParentTopic(creatorId, topicId);
    learnProgressContentElementRepository.persistAndFlush(
        newContentElement(creatorId, topicId, contentElementId));

    Optional<LearnProgressContentElement> result =
        learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, unique("other-creator"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByContentElementIdAndCreatorId_unknownContentElementId_returnsEmpty() {
    Optional<LearnProgressContentElement> result =
        learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            UUID.randomUUID(), unique("creator"));

    assertTrue(result.isEmpty());
  }
}
