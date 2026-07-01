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

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link TopicRepository} against a real Postgres instance (Quarkus dev services). Demo
 * content (176 topics with German titles) is loaded into the same database at application boot, so
 * every fixture title here is randomized to avoid unique-constraint collisions, and assertions on
 * search/popularity results only check that the fixture is present rather than asserting exact
 * result sets.
 */
@QuarkusTest
class TopicRepositoryTest {

  @Inject TopicRepository topicRepository;

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

  @Test
  @TestTransaction
  void findByTitleOptional_existingTitle_returnsTopic() {
    String title = unique("Quantum-Physics");
    Topic topic = newTopic(title);
    topicRepository.persistAndFlush(topic);

    Optional<Topic> result = topicRepository.findByTitleOptional(title);

    assertTrue(result.isPresent());
    assertEquals(topic.id, result.get().id);
  }

  @Test
  @TestTransaction
  void findByTitleOptional_noMatch_returnsEmpty() {
    Optional<Topic> result = topicRepository.findByTitleOptional(unique("DoesNotExist"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findForCreatorPaginated_returnsOnlyOwnTopicsPaginated() {
    String creatorId = unique("creator");
    Topic a = newTopic(unique("Pagination-A"));
    Topic b = newTopic(unique("Pagination-B"));
    Topic c = newTopic(unique("Pagination-C"));
    a.creatorId = creatorId;
    b.creatorId = creatorId;
    c.creatorId = creatorId;
    topicRepository.persistAndFlush(a);
    topicRepository.persistAndFlush(b);
    topicRepository.persistAndFlush(c);

    Topic other = newTopic(unique("Pagination-Other"));
    other.creatorId = unique("other-creator");
    topicRepository.persistAndFlush(other);

    PaginationDto<Topic> firstPage = topicRepository.findForCreatorPaginated(creatorId, 0, 2);
    PaginationDto<Topic> secondPage = topicRepository.findForCreatorPaginated(creatorId, 1, 2);

    assertEquals(2, firstPage.results.size());
    assertEquals(1, secondPage.results.size());
    assertEquals(2, firstPage.totalPages);
    assertTrue(firstPage.results.stream().allMatch(t -> t.creatorId.equals(creatorId)));
    assertTrue(secondPage.results.stream().allMatch(t -> t.creatorId.equals(creatorId)));
    List<UUID> allIds =
        List.of(
            firstPage.results.get(0).id, firstPage.results.get(1).id, secondPage.results.get(0).id);
    assertTrue(allIds.stream().noneMatch(id -> id.equals(other.id)));
  }

  @Test
  @TestTransaction
  void findBySearch_distinctiveGermanWord_includesFixtureTopic() {
    String marker = unique("Marker").replace("-", "");
    Topic topic = newTopic("Programmierung " + marker);
    topic.teaser = "Ein Kurs ueber Softwareentwicklung und " + marker;
    topicRepository.persistAndFlush(topic);
    topicRepository.getEntityManager().clear();

    List<Topic> result = topicRepository.findBySearch("Programmierung", 100);

    assertTrue(result.stream().anyMatch(t -> t.id.equals(topic.id)));
  }

  @Test
  @TestTransaction
  void findBySearch_nonsenseTerm_doesNotIncludeFixtureTopic() {
    String marker = unique("Marker").replace("-", "");
    Topic topic = newTopic("Programmierung " + marker);
    topic.teaser = "Ein Kurs ueber Softwareentwicklung und " + marker;
    topicRepository.persistAndFlush(topic);
    topicRepository.getEntityManager().clear();

    String nonsense = "zzznonexistentword" + UUID.randomUUID().toString().replace("-", "");
    List<Topic> result = topicRepository.findBySearch(nonsense, 100);

    assertTrue(result.stream().noneMatch(t -> t.id.equals(topic.id)));
  }

  @Test
  @TestTransaction
  void findBySearch_blankSearchTerm_doesNotThrowAndReturnsNoMatch() {
    // Exercises the safeGermanPrefixSearch null/blank guard, which short-circuits to an empty
    // tsquery string instead of forwarding whitespace-only input to to_tsquery.
    String marker = unique("Marker").replace("-", "");
    Topic topic = newTopic("Programmierung " + marker);
    topicRepository.persistAndFlush(topic);
    topicRepository.getEntityManager().clear();

    List<Topic> result = topicRepository.findBySearch("   ", 100);

    assertTrue(result.stream().noneMatch(t -> t.id.equals(topic.id)));
  }

  @Test
  @TestTransaction
  void findByOwningTopicId_returnsForeignTopicOfThatOwner() {
    // The query joins Topic.foreignAssociations -> owningTopic and filters on owningTopic.id, so
    // it returns the topics on the FOREIGN side of associations owned by the given topic id.
    Topic owning = newTopic(unique("Owning"));
    Topic foreign = newTopic(unique("Foreign"));

    TopicAssociation association = new TopicAssociation();
    association.owningTopic = owning;
    association.foreignTopic = foreign;
    association.creatorId = "tester";
    topicAssociationRepository.persistAndFlush(association);

    List<Topic> result = topicRepository.findByOwningTopicId(owning.id);

    assertTrue(result.stream().anyMatch(t -> t.id.equals(foreign.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(owning.id)));
  }

  @Test
  @TestTransaction
  void findByOwningTopicId_noAssociations_returnsEmpty() {
    Topic lonely = newTopic(unique("Lonely"));
    topicRepository.persistAndFlush(lonely);

    List<Topic> result = topicRepository.findByOwningTopicId(lonely.id);

    assertTrue(result.isEmpty());
  }
}
