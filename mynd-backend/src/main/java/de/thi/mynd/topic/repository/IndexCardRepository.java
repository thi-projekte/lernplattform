package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.IndexCard;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class IndexCardRepository extends MyndBaseRepository<IndexCard> {

    public List<IndexCard> findByTopicId(UUID topicId) {
        return find("topic.id = ?1", topicId).list();
    }
}
