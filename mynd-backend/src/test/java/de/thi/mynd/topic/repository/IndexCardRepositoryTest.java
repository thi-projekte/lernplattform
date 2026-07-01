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

import de.thi.mynd.topic.entity.IndexCard;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link IndexCardRepository} against a real Postgres instance (Quarkus dev services).
 * {@link IndexCard#topic} has no cascade, so the referenced {@link Topic} must be persisted first.
 * {@link Topic#title} is DB-unique, so every fixture topic title is randomized.
 */
@QuarkusTest
class IndexCardRepositoryTest {

  @Inject IndexCardRepository indexCardRepository;

  @Inject TopicRepository topicRepository;

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

  private IndexCard newIndexCard(Topic topic, String question, String answer) {
    IndexCard card = new IndexCard();
    card.topic = topic;
    card.question = question;
    card.answer = answer;
    card.creatorId = "tester";
    return card;
  }

  @Test
  @TestTransaction
  void findByTopicId_returnsOnlyCardsForThatTopic() {
    Topic topicA = newTopic(unique("Topic-A"));
    Topic topicB = newTopic(unique("Topic-B"));
    topicRepository.persistAndFlush(topicA);
    topicRepository.persistAndFlush(topicB);

    IndexCard cardA1 = newIndexCard(topicA, "Question A1?", "Answer A1");
    IndexCard cardA2 = newIndexCard(topicA, "Question A2?", "Answer A2");
    IndexCard cardB1 = newIndexCard(topicB, "Question B1?", "Answer B1");
    indexCardRepository.persistAndFlush(cardA1);
    indexCardRepository.persistAndFlush(cardA2);
    indexCardRepository.persistAndFlush(cardB1);

    List<IndexCard> result = indexCardRepository.findByTopicId(topicA.id);

    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(c -> c.topic.id.equals(topicA.id)));
    assertTrue(result.stream().noneMatch(c -> c.id.equals(cardB1.id)));
  }

  @Test
  @TestTransaction
  void findByTopicId_topicWithNoCards_returnsEmpty() {
    Topic topic = newTopic(unique("Empty-Topic"));
    topicRepository.persistAndFlush(topic);

    List<IndexCard> result = indexCardRepository.findByTopicId(topic.id);

    assertTrue(result.isEmpty());
  }
}
