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

import de.thi.mynd.topic.dto.content.YouTubeLinkElementDto;
import de.thi.mynd.topic.entity.YouTubeLinkElement;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class YouTubeLinkElementDtoMapperTest {

  @Inject YouTubeLinkElementDtoMapper youTubeLinkElementDtoMapper;

  private YouTubeLinkElement youTubeLinkElement() {
    YouTubeLinkElement element = new YouTubeLinkElement();
    element.id = UUID.randomUUID();
    element.title = "Tutorial Video";
    element.icon = "video";
    element.rank = 4;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.uri = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    YouTubeLinkElement element = youTubeLinkElement();

    YouTubeLinkElementDto dto = youTubeLinkElementDtoMapper.mapAndEnrich(element);

    assertEquals(element.id, dto.id);
    assertEquals(element.title, dto.title);
    assertEquals(element.icon, dto.icon);
    assertEquals(element.rank, dto.rank);
    assertEquals(element.createdAt, dto.createdAt);
    assertEquals(element.updatedAt, dto.updatedAt);
    assertEquals(element.uri, dto.uri);
  }

  @Test
  void getEntityType_returnsYouTubeLinkElement() {
    assertEquals(YouTubeLinkElement.class, youTubeLinkElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsYouTubeLinkElementDto() {
    assertEquals(YouTubeLinkElementDto.class, youTubeLinkElementDtoMapper.getDtoType());
  }
}
