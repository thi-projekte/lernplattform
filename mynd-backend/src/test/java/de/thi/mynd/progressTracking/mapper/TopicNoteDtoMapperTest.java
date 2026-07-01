/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.entity.TopicNoteId;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TopicNoteDtoMapperTest {

  @Inject TopicNoteDtoMapper topicNoteDtoMapper;

  private TopicNote topicNote() {
    TopicNoteId id = new TopicNoteId();
    id.topicId = UUID.randomUUID();
    id.creatorId = "alice";

    TopicNote note = new TopicNote();
    note.id = id;
    note.content = "Some interesting note content";
    return note;
  }

  @Test
  void mapAndEnrich_copiesContent() {
    TopicNote note = topicNote();

    TopicNoteDto dto = topicNoteDtoMapper.mapAndEnrich(note);

    assertEquals(note.content, dto.content);
  }

  @Test
  void getEntityType_returnsTopicNote() {
    assertEquals(TopicNote.class, topicNoteDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsTopicNoteDto() {
    assertEquals(TopicNoteDto.class, topicNoteDtoMapper.getDtoType());
  }
}
