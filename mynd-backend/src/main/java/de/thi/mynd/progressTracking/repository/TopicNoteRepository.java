package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.entity.TopicNoteId;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class TopicNoteRepository extends MyndBaseCustomIdRepository<TopicNote, TopicNoteId> {
}
