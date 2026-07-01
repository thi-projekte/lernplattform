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

import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.entity.IndexCard;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IndexCardDtoMapperTest {

  @Inject IndexCardDtoMapper indexCardDtoMapper;

  private IndexCard indexCard() {
    IndexCard indexCard = new IndexCard();
    indexCard.id = UUID.randomUUID();
    indexCard.question = "What is the capital of France?";
    indexCard.answer = "Paris";
    return indexCard;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    IndexCard indexCard = indexCard();

    IndexCardDto dto = indexCardDtoMapper.mapAndEnrich(indexCard);

    assertEquals(indexCard.id, dto.id);
    assertEquals(indexCard.question, dto.question);
    assertEquals(indexCard.answer, dto.answer);
  }

  @Test
  void getEntityType_returnsIndexCard() {
    assertEquals(IndexCard.class, indexCardDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsIndexCardDto() {
    assertEquals(IndexCardDto.class, indexCardDtoMapper.getDtoType());
  }
}
