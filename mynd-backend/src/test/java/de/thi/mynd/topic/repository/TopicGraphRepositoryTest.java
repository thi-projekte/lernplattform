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

import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import de.thi.mynd.progressTracking.repository.LearnProgressTopicRepository;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link TopicGraphRepository} against a real Postgres instance (Quarkus dev services).
 * Demo content (176 topics) is loaded at boot with the default {@code popularityScore} of 0, so
 * fixture topics here use very high scores (near {@link Integer#MAX_VALUE}) to deterministically
 * sort above any pre-existing data, and {@link Topic#title} is randomized to avoid the DB-unique
 * constraint.
 */
@QuarkusTest
class TopicGraphRepositoryTest {

  @Inject TopicGraphRepository topicGraphRepository;

  @Inject TopicAssociationRepository topicAssociationRepository;

  @Inject LearnProgressTopicRepository learnProgressTopicRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private Topic newTopic(String title, int popularityScore) {
    Topic t = new Topic();
    t.title = title;
    t.teaser = "Teaser for " + title;
    t.creatorId = "tester";
    t.popularityScore = popularityScore;
    return t;
  }

  private TopicAssociation newAssociation(Topic owner, Topic foreign) {
    TopicAssociation association = new TopicAssociation();
    association.owningTopic = owner;
    association.foreignTopic = foreign;
    association.creatorId = "tester";
    return association;
  }

  private LearnProgressTopic newProgress(
      Topic topic, String creatorId, LearnProgressStatus status) {
    LearnProgressTopic progress = new LearnProgressTopic();
    LearnProgressTopicId id = new LearnProgressTopicId();
    id.creatorId = creatorId;
    id.topicId = topic.id;
    progress.id = id;
    progress.status = status;
    progress.contentElementsToComplete = 0;
    return progress;
  }

  @Test
  @TestTransaction
  void findNMostPopular_ordersDescendingAndRespectsLimit() {
    Topic high = newTopic(unique("High"), Integer.MAX_VALUE);
    Topic mid = newTopic(unique("Mid"), Integer.MAX_VALUE - 1);
    Topic low = newTopic(unique("Low"), Integer.MAX_VALUE - 2);
    topicGraphRepository.persistAndFlush(low);
    topicGraphRepository.persistAndFlush(high);
    topicGraphRepository.persistAndFlush(mid);

    List<Topic> result = topicGraphRepository.findNMostPopular(2);

    assertEquals(2, result.size());
    assertEquals(high.id, result.get(0).id);
    assertEquals(mid.id, result.get(1).id);
  }

  @Test
  @TestTransaction
  void findNMostPopular_withCreatorId_filtersByCreator() {
    String creatorA = unique("creatorA");
    String creatorB = unique("creatorB");
    Topic aHigh = newTopic(unique("A-High"), Integer.MAX_VALUE);
    aHigh.creatorId = creatorA;
    Topic aLow = newTopic(unique("A-Low"), Integer.MAX_VALUE - 1);
    aLow.creatorId = creatorA;
    Topic bHigh = newTopic(unique("B-High"), Integer.MAX_VALUE);
    bHigh.creatorId = creatorB;
    topicGraphRepository.persistAndFlush(aHigh);
    topicGraphRepository.persistAndFlush(aLow);
    topicGraphRepository.persistAndFlush(bHigh);

    List<Topic> result = topicGraphRepository.findNMostPopular(50, creatorA);

    assertTrue(result.stream().allMatch(t -> t.creatorId.equals(creatorA)));
    assertTrue(result.stream().anyMatch(t -> t.id.equals(aHigh.id)));
    assertTrue(result.stream().anyMatch(t -> t.id.equals(aLow.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(bHigh.id)));
    assertEquals(aHigh.id, result.get(0).id);
    assertEquals(aLow.id, result.get(1).id);
  }

  @Test
  @TestTransaction
  void findNMostPopularFilterByCategoryIds_returnsOnlyTopicsWithMatchingCategory() {
    Category category = new Category();
    category.title = unique("Category");
    category.color = "#000000";
    category.creatorId = "tester";

    Topic categorized = newTopic(unique("Categorized"), Integer.MAX_VALUE);
    categorized.categories.add(category);
    Topic uncategorized = newTopic(unique("Uncategorized"), Integer.MAX_VALUE);
    topicGraphRepository.persistAndFlush(categorized);
    topicGraphRepository.persistAndFlush(uncategorized);

    List<Topic> result =
        topicGraphRepository.findNMostPopularFilterByCategoryIds(50, List.of(category.id));

    assertTrue(result.stream().anyMatch(t -> t.id.equals(categorized.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(uncategorized.id)));
  }

  @Test
  @TestTransaction
  void findNMostPopularFilterByCategoryIds_withCreatorId_filtersByCategoryAndCreator() {
    Category category = new Category();
    category.title = unique("Category");
    category.color = "#000000";
    category.creatorId = "tester";

    String creatorA = unique("creatorA");
    String creatorB = unique("creatorB");

    Topic ownCategorized = newTopic(unique("OwnCategorized"), Integer.MAX_VALUE);
    ownCategorized.creatorId = creatorA;
    ownCategorized.categories.add(category);

    Topic otherCategorized = newTopic(unique("OtherCategorized"), Integer.MAX_VALUE);
    otherCategorized.creatorId = creatorB;
    otherCategorized.categories.add(category);

    Topic ownUncategorized = newTopic(unique("OwnUncategorized"), Integer.MAX_VALUE);
    ownUncategorized.creatorId = creatorA;

    topicGraphRepository.persistAndFlush(ownCategorized);
    topicGraphRepository.persistAndFlush(otherCategorized);
    topicGraphRepository.persistAndFlush(ownUncategorized);

    List<Topic> result =
        topicGraphRepository.findNMostPopularFilterByCategoryIds(
            50, List.of(category.id), creatorA);

    assertTrue(result.stream().anyMatch(t -> t.id.equals(ownCategorized.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(otherCategorized.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(ownUncategorized.id)));
  }

  @Test
  @TestTransaction
  void findForAssociatedTopicsNotStartedAllCompleted_returnsCompletedAssociatedTopic() {
    String creatorId = unique("creator");
    Topic seed = newTopic(unique("Seed"), 0);
    Topic completedNeighbor = newTopic(unique("CompletedNeighbor"), 0);
    Topic notStartedNeighbor = newTopic(unique("NotStartedNeighbor"), 0);

    topicAssociationRepository.persistAndFlush(newAssociation(seed, completedNeighbor));
    topicAssociationRepository.persistAndFlush(newAssociation(seed, notStartedNeighbor));

    learnProgressTopicRepository.persistAndFlush(
        newProgress(completedNeighbor, creatorId, LearnProgressStatus.COMPLETED_MANUALLY));

    List<Topic> result =
        topicGraphRepository.findForAssociatedTopicsNotStartedAllCompleted(
            List.of(seed.id), creatorId);

    assertTrue(result.stream().anyMatch(t -> t.id.equals(completedNeighbor.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(notStartedNeighbor.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(seed.id)));
  }

  @Test
  @TestTransaction
  void findUncompletedNeighborsOf_returnsOnlyUncompletedNeighbors() {
    String creatorId = unique("creator");
    Topic seed = newTopic(unique("Seed"), 0);
    Topic uncompletedNeighbor = newTopic(unique("UncompletedNeighbor"), 0);
    Topic completedNeighbor = newTopic(unique("CompletedNeighbor"), 0);

    topicAssociationRepository.persistAndFlush(newAssociation(seed, uncompletedNeighbor));
    topicAssociationRepository.persistAndFlush(newAssociation(seed, completedNeighbor));

    learnProgressTopicRepository.persistAndFlush(
        newProgress(completedNeighbor, creatorId, LearnProgressStatus.COMPLETED_MANUALLY));

    List<Topic> result =
        topicGraphRepository.findUncompletedNeighborsOf(List.of(seed.id), creatorId);

    assertTrue(result.stream().anyMatch(t -> t.id.equals(uncompletedNeighbor.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(completedNeighbor.id)));
    assertTrue(result.stream().noneMatch(t -> t.id.equals(seed.id)));
  }
}
