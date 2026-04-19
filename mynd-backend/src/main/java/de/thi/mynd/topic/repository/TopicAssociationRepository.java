package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.TopicAssociation;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicAssociationRepository extends MyndBaseRepository<TopicAssociation> {

    public List<TopicAssociation> findOwningAssociationsByIdsAndUsername(UUID topicId, List<UUID> ids, String username) {
        return find("owningTopic = ?1 AND creatorId = ?1 AND foreignTopic IN ?3", topicId, username, ids).list();
    }
}
