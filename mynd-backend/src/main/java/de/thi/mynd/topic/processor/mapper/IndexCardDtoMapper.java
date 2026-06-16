/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.entity.IndexCard;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class IndexCardDtoMapper extends AbstractMappingProcessor<IndexCard, IndexCardDto> {
  @Override
  public IndexCardDto mapAndEnrich(IndexCard entity) {
    return IndexCardDto.builder()
        .id(entity.id)
        .question(entity.question)
        .answer(entity.answer)
        .build();
  }

  @Override
  public Class<IndexCard> getEntityType() {
    return IndexCard.class;
  }

  @Override
  public Class<IndexCardDto> getDtoType() {
    return IndexCardDto.class;
  }
}
