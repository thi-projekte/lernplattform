/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.request.TopicNoteRequest;
import java.util.UUID;

public interface TopicNoteService {

  TopicNoteDto getTopicNoteForCurrentUser(UUID topicId);

  TopicNoteDto updateTopicNoteForCurrentUser(UUID topicId, TopicNoteRequest request);

  TopicNote createDefaultForCurrentUser(UUID topicId);
}
