package de.thi.mynd.progressTracking.service;

import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.request.TopicNoteRequest;

import java.util.UUID;

public interface TopicNoteService {

    TopicNoteDto getTopicNoteForCurrentUser(UUID topicId);

    TopicNoteDto updateTopicNoteForCurrentUser(UUID topicId, TopicNoteRequest request);
}
