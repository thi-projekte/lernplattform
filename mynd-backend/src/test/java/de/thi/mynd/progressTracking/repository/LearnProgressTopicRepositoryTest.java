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

import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link LearnProgressTopicRepository} against a real Postgres instance (Quarkus dev
 * services).
 */
@QuarkusTest
class LearnProgressTopicRepositoryTest {

  @Inject LearnProgressTopicRepository learnProgressTopicRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private LearnProgressTopic newLearnProgressTopic(String creatorId, UUID topicId) {
    LearnProgressTopic t = new LearnProgressTopic();
    LearnProgressTopicId id = new LearnProgressTopicId();
    id.creatorId = creatorId;
    id.topicId = topicId;
    t.id = id;
    t.creatorId = creatorId;
    t.status = LearnProgressStatus.STARTED;
    t.contentElementsToComplete = 5;
    return t;
  }

  @Test
  @TestTransaction
  void findOneByTopicIdAndCreatorIdContentElementsFetched_existingRow_isFound() {
    String creatorId = unique("creator");
    UUID topicId = UUID.randomUUID();
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicId));

    Optional<LearnProgressTopic> result =
        learnProgressTopicRepository.findOneByTopicIdAndCreatorIdContentElementsFetched(
            topicId, creatorId);

    assertTrue(result.isPresent());
    assertEquals(topicId, result.get().id.topicId);
    assertEquals(creatorId, result.get().id.creatorId);
  }

  @Test
  @TestTransaction
  void findOneByTopicIdAndCreatorIdContentElementsFetched_noMatch_returnsEmpty() {
    Optional<LearnProgressTopic> result =
        learnProgressTopicRepository.findOneByTopicIdAndCreatorIdContentElementsFetched(
            UUID.randomUUID(), unique("creator"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByTopicIdsAndCreatorIdContentElementsFetched_returnsMatchingTopics() {
    String creatorId = unique("creator");
    UUID topicIdA = UUID.randomUUID();
    UUID topicIdB = UUID.randomUUID();
    UUID topicIdC = UUID.randomUUID();
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicIdA));
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicIdB));
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicIdC));

    List<LearnProgressTopic> result =
        learnProgressTopicRepository.findByTopicIdsAndCreatorIdContentElementsFetched(
            List.of(topicIdA, topicIdC), creatorId);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(t -> t.id.topicId.equals(topicIdA)));
    assertTrue(result.stream().anyMatch(t -> t.id.topicId.equals(topicIdC)));
    assertTrue(result.stream().noneMatch(t -> t.id.topicId.equals(topicIdB)));
  }

  @Test
  @TestTransaction
  void findByTopicIdsAndCreatorIdContentElementsFetched_emptyIds_returnsEmpty() {
    List<LearnProgressTopic> result =
        learnProgressTopicRepository.findByTopicIdsAndCreatorIdContentElementsFetched(
            List.of(), unique("creator"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void getLastNLearnedTopicIdsForUser_ordersByUpdatedAtAndRespectsLimit() throws Exception {
    String creatorId = unique("creator");
    UUID topicIdFirst = UUID.randomUUID();
    UUID topicIdSecond = UUID.randomUUID();
    UUID topicIdThird = UUID.randomUUID();
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicIdFirst));
    Thread.sleep(5);
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicIdSecond));
    Thread.sleep(5);
    learnProgressTopicRepository.persistAndFlush(newLearnProgressTopic(creatorId, topicIdThird));

    List<UUID> result = learnProgressTopicRepository.getLastNLearnedTopicIdsForUser(2, creatorId);

    assertEquals(2, result.size());
    assertEquals(topicIdFirst, result.get(0));
    assertEquals(topicIdSecond, result.get(1));
  }

  @Test
  @TestTransaction
  void getLastNLearnedTopicIdsForUser_noRows_returnsEmpty() {
    List<UUID> result =
        learnProgressTopicRepository.getLastNLearnedTopicIdsForUser(5, unique("creator"));

    assertTrue(result.isEmpty());
  }
}
