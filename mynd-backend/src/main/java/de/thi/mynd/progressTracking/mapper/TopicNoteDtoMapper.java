package de.thi.mynd.progressTracking.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.entity.TopicNote;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class TopicNoteDtoMapper extends AbstractMappingProcessor<TopicNote, TopicNoteDto> {
  @Override
  public TopicNoteDto mapAndEnrich(TopicNote entity) {
    return TopicNoteDto.builder().content(entity.content).build();
  }

  @Override
  public Class<TopicNote> getEntityType() {
    return TopicNote.class;
  }

  @Override
  public Class<TopicNoteDto> getDtoType() {
    return TopicNoteDto.class;
  }
}
