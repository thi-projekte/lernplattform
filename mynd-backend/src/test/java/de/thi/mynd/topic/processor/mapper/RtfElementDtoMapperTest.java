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

import de.thi.mynd.topic.dto.content.RtfElementDto;
import de.thi.mynd.topic.entity.RtfElement;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RtfElementDtoMapperTest {

  @Inject RtfElementDtoMapper rtfElementDtoMapper;

  private RtfElement rtfElement() {
    RtfElement element = new RtfElement();
    element.id = UUID.randomUUID();
    element.title = "Notes";
    element.icon = "note";
    element.rank = 2;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.rtfText = "{\\rtf1 some rich text}";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    RtfElement element = rtfElement();

    RtfElementDto dto = rtfElementDtoMapper.mapAndEnrich(element);

    assertEquals(element.id, dto.id);
    assertEquals(element.title, dto.title);
    assertEquals(element.icon, dto.icon);
    assertEquals(element.rank, dto.rank);
    assertEquals(element.createdAt, dto.createdAt);
    assertEquals(element.updatedAt, dto.updatedAt);
    assertEquals(element.rtfText, dto.rtfText);
  }

  @Test
  void getEntityType_returnsRtfElement() {
    assertEquals(RtfElement.class, rtfElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsRtfElementDto() {
    assertEquals(RtfElementDto.class, rtfElementDtoMapper.getDtoType());
  }
}
