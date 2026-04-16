package de.thi.mynd.topic.repository;

import de.thi.mynd.topic.entity.ContentElement;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class ContentElementRepository implements PanacheRepository<ContentElement> {

    public List<ContentElement> findForTopic(UUID topicId) {
        return find("topic.id = ?1", topicId).list();
    }
}
