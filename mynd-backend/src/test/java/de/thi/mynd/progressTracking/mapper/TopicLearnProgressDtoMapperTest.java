/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.entity.LearnProgressContentElement;
import de.thi.mynd.progressTracking.entity.LearnProgressContentElementId;
import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicLearnProgressDtoMapperTest {

  @Inject TopicLearnProgressDtoMapper topicLearnProgressDtoMapper;

  private static final String CREATOR_ID = "alice";

  private LearnProgressContentElement contentElement(UUID topicId, boolean completed) {
    LearnProgressContentElementId id = new LearnProgressContentElementId();
    id.creatorId = CREATOR_ID;
    id.topicId = topicId;
    id.contentElementId = UUID.randomUUID();

    LearnProgressContentElement element = new LearnProgressContentElement();
    element.id = id;
    element.completed = completed;
    return element;
  }

  private LearnProgressTopic topic(
      LearnProgressStatus status,
      long contentElementsToComplete,
      List<LearnProgressContentElement> elements) {
    LearnProgressTopicId id = new LearnProgressTopicId();
    id.creatorId = CREATOR_ID;
    id.topicId = UUID.randomUUID();

    LearnProgressTopic topic = new LearnProgressTopic();
    topic.id = id;
    topic.status = status;
    topic.contentElementsToComplete = contentElementsToComplete;
    topic.contentElements = elements;
    return topic;
  }

  @Test
  void mapAndEnrich_noElementsCompleted_percentageIsZero() {
    LearnProgressTopic topic =
        topic(
            LearnProgressStatus.STARTED,
            3,
            new ArrayList<>(
                List.of(
                    contentElement(null, false),
                    contentElement(null, false),
                    contentElement(null, false))));

    TopicLearnProgressDto dto = topicLearnProgressDtoMapper.mapAndEnrich(topic);

    assertEquals(topic.id.topicId, dto.topicId);
    assertEquals(LearnProgressStatus.STARTED, dto.status);
    assertFalse(dto.completed);
    assertTrue(dto.completedContentElementIds.isEmpty());
    assertEquals(0.0, dto.percentageCompleted);
  }

  @Test
  void mapAndEnrich_someElementsCompleted_computesPartialPercentageAndIds() {
    LearnProgressContentElement completed1 = contentElement(null, true);
    LearnProgressContentElement completed2 = contentElement(null, true);
    LearnProgressContentElement notCompleted = contentElement(null, false);

    LearnProgressTopic topic =
        topic(
            LearnProgressStatus.STARTED,
            5,
            new ArrayList<>(List.of(completed1, completed2, notCompleted)));

    TopicLearnProgressDto dto = topicLearnProgressDtoMapper.mapAndEnrich(topic);

    assertFalse(dto.completed);
    assertEquals(2, dto.completedContentElementIds.size());
    assertTrue(dto.completedContentElementIds.contains(completed1.id.contentElementId));
    assertTrue(dto.completedContentElementIds.contains(completed2.id.contentElementId));
    assertEquals(2.0 / 5.0, dto.percentageCompleted);
  }

  @Test
  void mapAndEnrich_allElementsCompletedStatus_percentageIsOneAndCompletedIsTrue() {
    LearnProgressContentElement completed1 = contentElement(null, true);
    LearnProgressContentElement completed2 = contentElement(null, true);

    LearnProgressTopic topic =
        topic(
            LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED,
            2,
            new ArrayList<>(List.of(completed1, completed2)));

    TopicLearnProgressDto dto = topicLearnProgressDtoMapper.mapAndEnrich(topic);

    assertTrue(dto.completed);
    assertEquals(1.0, dto.percentageCompleted);
  }

  @Test
  void mapAndEnrich_manuallyCompletedStatus_completedIsTrueViaOr() {
    LearnProgressTopic topic = topic(LearnProgressStatus.COMPLETED_MANUALLY, 4, new ArrayList<>());

    TopicLearnProgressDto dto = topicLearnProgressDtoMapper.mapAndEnrich(topic);

    assertTrue(dto.completed);
  }

  @Test
  void mapAndEnrich_startedStatus_completedIsFalse() {
    LearnProgressTopic topic = topic(LearnProgressStatus.STARTED, 4, new ArrayList<>());

    TopicLearnProgressDto dto = topicLearnProgressDtoMapper.mapAndEnrich(topic);

    assertFalse(dto.completed);
  }

  /**
   * Edge case: when contentElementsToComplete is 0 (e.g. a topic with no content elements at all),
   * the mapper does not guard against the divide-by-zero and computes 0.0 / 0.0, which is NaN in
   * Java's double arithmetic. This is the actual observed behavior of {@link
   * TopicLearnProgressDtoMapper#mapAndEnrich(LearnProgressTopic)} today — it is asserted here as
   * documentation, not as a statement that this is desirable. Consumers of percentageCompleted
   * should be aware that NaN can be produced for topics with zero required content elements.
   */
  @Test
  void mapAndEnrich_zeroContentElementsToComplete_percentageIsNaN() {
    LearnProgressTopic topic = topic(LearnProgressStatus.STARTED, 0, new ArrayList<>());

    TopicLearnProgressDto dto = topicLearnProgressDtoMapper.mapAndEnrich(topic);

    assertTrue(Double.isNaN(dto.percentageCompleted));
  }

  @Test
  void getEntityType_returnsLearnProgressTopic() {
    assertEquals(LearnProgressTopic.class, topicLearnProgressDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsTopicLearnProgressDto() {
    assertEquals(TopicLearnProgressDto.class, topicLearnProgressDtoMapper.getDtoType());
  }
}
