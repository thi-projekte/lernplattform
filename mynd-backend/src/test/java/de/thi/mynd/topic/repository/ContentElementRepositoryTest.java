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

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.UriElement;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link ContentElementRepository} against a real Postgres instance (Quarkus dev
 * services). {@link ContentElement} uses joined-table inheritance with a discriminator, so {@link
 * UriElement} is used as the concrete subclass under test; its {@link UriElement#topic} relation
 * cascades persistence, so a fresh {@link Topic} is created automatically. {@link Topic#title} is
 * DB-unique, so every fixture topic title is randomized.
 */
@QuarkusTest
class ContentElementRepositoryTest {

  @Inject ContentElementRepository contentElementRepository;

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

  private UriElement newUriElement(Topic topic, String title) {
    UriElement element = new UriElement();
    element.title = title;
    element.topic = topic;
    element.uri = "https://example.com/" + UUID.randomUUID();
    element.creatorId = "tester";
    return element;
  }

  @Test
  @TestTransaction
  void findForTopic_returnsOnlyElementsForThatTopic() {
    Topic topicA = newTopic(unique("Topic-A"));
    Topic topicB = newTopic(unique("Topic-B"));

    UriElement elementA1 = newUriElement(topicA, unique("Element-A1"));
    UriElement elementA2 = newUriElement(topicA, unique("Element-A2"));
    UriElement elementB1 = newUriElement(topicB, unique("Element-B1"));
    contentElementRepository.persistAndFlush(elementA1);
    contentElementRepository.persistAndFlush(elementA2);
    contentElementRepository.persistAndFlush(elementB1);

    List<ContentElement> result = contentElementRepository.findForTopic(topicA.id);

    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(e -> e.topic.id.equals(topicA.id)));
    assertTrue(result.stream().noneMatch(e -> e.id.equals(elementB1.id)));
  }

  @Test
  @TestTransaction
  void findForTopic_topicWithNoElements_returnsEmpty() {
    UriElement otherElement = newUriElement(newTopic(unique("Other-Topic")), unique("Other"));
    contentElementRepository.persistAndFlush(otherElement);

    List<ContentElement> result = contentElementRepository.findForTopic(UUID.randomUUID());

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void countForTopic_countsOnlyElementsForThatTopic() {
    Topic topicA = newTopic(unique("Topic-A"));
    Topic topicB = newTopic(unique("Topic-B"));

    contentElementRepository.persistAndFlush(newUriElement(topicA, unique("Element-A1")));
    contentElementRepository.persistAndFlush(newUriElement(topicA, unique("Element-A2")));
    contentElementRepository.persistAndFlush(newUriElement(topicA, unique("Element-A3")));
    contentElementRepository.persistAndFlush(newUriElement(topicB, unique("Element-B1")));

    assertEquals(3, contentElementRepository.countForTopic(topicA.id));
    assertEquals(1, contentElementRepository.countForTopic(topicB.id));
  }

  @Test
  @TestTransaction
  void countForTopic_noElements_returnsZero() {
    assertEquals(0, contentElementRepository.countForTopic(UUID.randomUUID()));
  }
}
