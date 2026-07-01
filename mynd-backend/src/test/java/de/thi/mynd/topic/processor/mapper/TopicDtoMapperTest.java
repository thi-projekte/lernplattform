/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.processor.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.service.IdentityService;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.dto.content.PdfElementDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.service.ContentElementService;
import de.thi.mynd.topic.service.IndexCardService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicDtoMapperTest {

  @Inject TopicDtoMapper topicDtoMapper;

  @InjectMock IdentityService identityService;

  @InjectMock ContentElementService contentElementService;

  @InjectMock IndexCardService indexCardService;

  private Topic topic() {
    Topic topic = new Topic();
    topic.id = UUID.randomUUID();
    topic.title = "Some Topic";
    topic.teaser = "Some Teaser";
    topic.creatorId = "alice";
    topic.estimatedLearningDuration = 42;
    topic.updatedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
    return topic;
  }

  @Test
  void mapAndEnrich_happyPath_mapsAllFieldsIncludingDelegatedCollections() {
    Topic topic = topic();
    List<ContentElementDto> contentElements = List.of(PdfElementDto.builder().build());
    List<IndexCardDto> indexCards = List.of(IndexCardDto.builder().id(UUID.randomUUID()).build());

    when(identityService.getFullNameByUsername("alice")).thenReturn("Alice Doe");
    when(contentElementService.getContentElementsForTopic(topic.id)).thenReturn(contentElements);
    when(indexCardService.getIndexCardsForTopic(topic.id)).thenReturn(indexCards);

    TopicDto dto = topicDtoMapper.mapAndEnrich(topic);

    assertEquals(topic.id, dto.id);
    assertEquals(topic.title, dto.title);
    assertEquals(topic.teaser, dto.teaser);
    assertEquals(topic.creatorId, dto.creatorId);
    assertEquals("Alice Doe", dto.creatorFullName);
    assertEquals(topic.estimatedLearningDuration, dto.estimatedLearningDuration);
    assertEquals(topic.categories, dto.categories);
    assertEquals(contentElements, dto.contentElements);
    assertEquals(indexCards, dto.indexCards);
    assertEquals(topic.updatedAt, dto.updatedAt);
    assertNull(dto.learnProgress);
  }

  @Test
  void mapAndEnrichWithAdditionalData_progressMapContainsTopic_setsLearnProgress() {
    Topic topic = topic();
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");
    when(contentElementService.getContentElementsForTopic(topic.id)).thenReturn(List.of());
    when(indexCardService.getIndexCardsForTopic(topic.id)).thenReturn(List.of());
    TopicLearnProgressDto progress = TopicLearnProgressDto.builder().topicId(topic.id).build();
    Map<UUID, TopicLearnProgressDto> progressMap = Map.of(topic.id, progress);

    TopicDto dto = topicDtoMapper.mapAndEnrich(topic, (Object) progressMap);

    assertEquals(progress, dto.learnProgress);
  }

  @Test
  void mapAndEnrichWithAdditionalData_progressMapMissesTopic_leavesLearnProgressNull() {
    Topic topic = topic();
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");
    when(contentElementService.getContentElementsForTopic(topic.id)).thenReturn(List.of());
    when(indexCardService.getIndexCardsForTopic(topic.id)).thenReturn(List.of());
    Map<UUID, TopicLearnProgressDto> progressMap = Map.of();

    TopicDto dto = topicDtoMapper.mapAndEnrich(topic, (Object) progressMap);

    assertNull(dto.learnProgress);
  }

  @Test
  void getEntityType_returnsTopic() {
    assertEquals(Topic.class, topicDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsTopicDto() {
    assertEquals(TopicDto.class, topicDtoMapper.getDtoType());
  }
}
