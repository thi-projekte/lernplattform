/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.entity.TopicNoteId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link TopicNoteRepository} against a real Postgres instance (Quarkus dev services).
 * This repository declares no custom query methods, and its primary key ({@link TopicNoteId}) is
 * not a {@code UUID}, so the inherited {@code findByIdsTypeSafe} (which is hard-coded to compare
 * against {@code UUID} ids) is not applicable here and is intentionally not exercised.
 */
@QuarkusTest
class TopicNoteRepositoryTest {

  @Inject TopicNoteRepository topicNoteRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private TopicNote newTopicNote(UUID topicId, String creatorId, String content) {
    TopicNote note = new TopicNote();
    TopicNoteId id = new TopicNoteId();
    id.topicId = topicId;
    id.creatorId = creatorId;
    note.id = id;
    note.creatorId = creatorId;
    note.content = content;
    return note;
  }

  @Test
  @TestTransaction
  void persistAndFindById_roundTripsEntity() {
    UUID topicId = UUID.randomUUID();
    String creatorId = unique("creator");
    TopicNote note = newTopicNote(topicId, creatorId, "Some note content");
    topicNoteRepository.persistAndFlush(note);

    TopicNoteId lookupId = new TopicNoteId();
    lookupId.topicId = topicId;
    lookupId.creatorId = creatorId;
    TopicNote found = topicNoteRepository.findById(lookupId);

    assertNotNull(found);
    assertEquals("Some note content", found.content);
  }

  @Test
  @TestTransaction
  void findById_missingId_returnsNull() {
    TopicNoteId lookupId = new TopicNoteId();
    lookupId.topicId = UUID.randomUUID();
    lookupId.creatorId = unique("missing");

    TopicNote found = topicNoteRepository.findById(lookupId);

    assertNull(found);
  }

  @Test
  @TestTransaction
  void findAllWithLimit_respectsLimit() {
    for (int i = 0; i < 5; i++) {
      topicNoteRepository.persistAndFlush(
          newTopicNote(UUID.randomUUID(), unique("creator-" + i), "content " + i));
    }

    List<TopicNote> result = topicNoteRepository.findAllWithLimit(3);

    assertEquals(3, result.size());
  }
}
