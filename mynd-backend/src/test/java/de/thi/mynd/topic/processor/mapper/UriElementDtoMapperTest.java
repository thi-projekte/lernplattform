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

import de.thi.mynd.topic.dto.content.UriElementDto;
import de.thi.mynd.topic.entity.UriElement;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UriElementDtoMapperTest {

  @Inject UriElementDtoMapper uriElementDtoMapper;

  private UriElement uriElement() {
    UriElement element = new UriElement();
    element.id = UUID.randomUUID();
    element.title = "Reference Link";
    element.icon = "link";
    element.rank = 1;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.uri = "https://example.com/reference";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    UriElement element = uriElement();

    UriElementDto dto = uriElementDtoMapper.mapAndEnrich(element);

    assertEquals(element.id, dto.id);
    assertEquals(element.title, dto.title);
    assertEquals(element.icon, dto.icon);
    assertEquals(element.rank, dto.rank);
    assertEquals(element.createdAt, dto.createdAt);
    assertEquals(element.updatedAt, dto.updatedAt);
    assertEquals(element.uri, dto.uri);
  }

  @Test
  void getEntityType_returnsUriElement() {
    assertEquals(UriElement.class, uriElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsUriElementDto() {
    assertEquals(UriElementDto.class, uriElementDtoMapper.getDtoType());
  }
}
