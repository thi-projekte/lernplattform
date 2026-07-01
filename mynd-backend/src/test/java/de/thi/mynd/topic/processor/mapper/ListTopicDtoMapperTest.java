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
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ListTopicDtoMapperTest {

  @Inject ListTopicDtoMapper listTopicDtoMapper;

  @InjectMock IdentityService identityService;

  private Topic topic() {
    Topic topic = new Topic();
    topic.id = UUID.randomUUID();
    topic.title = "Linear Algebra";
    topic.creatorId = "alice";
    topic.updatedAt = LocalDateTime.now();
    Category category = new Category();
    category.id = UUID.randomUUID();
    category.title = "Mathematics";
    topic.categories = List.of(category);
    return topic;
  }

  @Test
  void mapAndEnrich_copiesAllFieldsAndEnrichesCreatorFullName() {
    Topic topic = topic();
    when(identityService.getFullNameByUsername("alice")).thenReturn("Alice Doe");

    ListTopicDto dto = listTopicDtoMapper.mapAndEnrich(topic);

    assertEquals(topic.id, dto.id);
    assertEquals(topic.title, dto.title);
    assertEquals(topic.categories, dto.categories);
    assertEquals(topic.updatedAt, dto.updatedAt);
    assertEquals(topic.creatorId, dto.creatorId);
    assertEquals("Alice Doe", dto.creatorFullName);
    verify(identityService).getFullNameByUsername("alice");
  }

  @Test
  void getEntityType_returnsTopic() {
    assertEquals(Topic.class, listTopicDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsListTopicDto() {
    assertEquals(ListTopicDto.class, listTopicDtoMapper.getDtoType());
  }
}
