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
import de.thi.mynd.progressTracking.service.LearnProgressService;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GraphTopicDtoMapperTest {

  @Inject GraphTopicDtoMapper graphTopicDtoMapper;

  @InjectMock IdentityService identityService;

  @InjectMock LearnProgressService learnProgressService;

  private Topic topic() {
    Topic topic = new Topic();
    topic.id = UUID.randomUUID();
    topic.title = "Graph Topic";
    topic.creatorId = "alice";
    return topic;
  }

  private TopicAssociation associationFrom(Topic owning, Topic foreign) {
    TopicAssociation association = new TopicAssociation();
    association.owningTopic = owning;
    association.foreignTopic = foreign;
    return association;
  }

  @Test
  void mapAndEnrich_noAssociations_associatedTopicsIsEmpty() {
    Topic topic = topic();
    when(identityService.getFullNameByUsername("alice")).thenReturn("Alice Doe");

    GraphTopicDto dto = graphTopicDtoMapper.mapAndEnrich(topic);

    assertEquals(topic.id, dto.id);
    assertEquals("Alice Doe", dto.creatorFullName);
    assertTrue(dto.associatedTopics.isEmpty());
  }

  @Test
  void mapAndEnrich_ownedAssociationsOnly_collectsForeignTopicIds() {
    Topic topic = topic();
    Topic foreign = topic();
    topic.ownedAssociations.add(associationFrom(topic, foreign));
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");

    GraphTopicDto dto = graphTopicDtoMapper.mapAndEnrich(topic);

    assertEquals(List.of(foreign.id), dto.associatedTopics);
  }

  @Test
  void mapAndEnrich_foreignAssociationsOnly_collectsOwningTopicIds() {
    Topic topic = topic();
    Topic owner = topic();
    topic.foreignAssociations.add(associationFrom(owner, topic));
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");

    GraphTopicDto dto = graphTopicDtoMapper.mapAndEnrich(topic);

    assertEquals(List.of(owner.id), dto.associatedTopics);
  }

  @Test
  void mapAndEnrich_ownedAndForeignAssociations_collectsBoth() {
    Topic topic = topic();
    Topic foreign = topic();
    Topic owner = topic();
    topic.ownedAssociations.add(associationFrom(topic, foreign));
    topic.foreignAssociations.add(associationFrom(owner, topic));
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");

    GraphTopicDto dto = graphTopicDtoMapper.mapAndEnrich(topic);

    assertEquals(2, dto.associatedTopics.size());
    assertTrue(dto.associatedTopics.contains(foreign.id));
    assertTrue(dto.associatedTopics.contains(owner.id));
  }

  @Test
  void mapAndEnrichWithAdditionalData_progressMapContainsTopic_setsLearnProgress() {
    Topic topic = topic();
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");
    TopicLearnProgressDto progress = TopicLearnProgressDto.builder().topicId(topic.id).build();
    Map<UUID, TopicLearnProgressDto> progressMap = Map.of(topic.id, progress);

    GraphTopicDto dto = graphTopicDtoMapper.mapAndEnrich(topic, (Object) progressMap);

    assertEquals(progress, dto.learnProgress);
  }

  @Test
  void mapAndEnrichWithAdditionalData_progressMapMissesTopic_leavesLearnProgressNull() {
    Topic topic = topic();
    when(identityService.getFullNameByUsername(anyString())).thenReturn("Alice Doe");
    Map<UUID, TopicLearnProgressDto> progressMap = Map.of();

    GraphTopicDto dto = graphTopicDtoMapper.mapAndEnrich(topic, (Object) progressMap);

    assertNull(dto.learnProgress);
  }

  @Test
  void obtainAdditionalData_delegatesToLearnProgressServiceWithEntityIds() {
    Topic topicA = topic();
    Topic topicB = topic();
    Map<UUID, TopicLearnProgressDto> mapping =
        Map.of(topicA.id, TopicLearnProgressDto.builder().topicId(topicA.id).build());
    when(learnProgressService.getLearnProgressMappingForTopics(anyList())).thenReturn(mapping);

    Object[] result = graphTopicDtoMapper.obtainAdditionalData(List.of(topicA, topicB));

    assertEquals(1, result.length);
    assertEquals(mapping, result[0]);
    verify(learnProgressService)
        .getLearnProgressMappingForTopics(
            argThat(ids -> ids.contains(topicA.id) && ids.contains(topicB.id)));
  }

  @Test
  void getEntityType_returnsTopic() {
    assertEquals(Topic.class, graphTopicDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsGraphTopicDto() {
    assertEquals(GraphTopicDto.class, graphTopicDtoMapper.getDtoType());
  }
}
