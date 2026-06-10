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
