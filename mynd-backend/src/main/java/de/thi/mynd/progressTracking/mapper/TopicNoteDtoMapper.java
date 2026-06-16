/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
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
