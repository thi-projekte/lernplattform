package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicAssociationServiceImpl implements TopicAssociationService {

    @Inject
    TopicAssociationRepository topicAssociationRepository;

    @Override
    public List<TopicAssociation> findOrCreateOwningTopicAssociationsOwnedByUser(Topic topic, List<AssociatedEntityRequest> associatedTopics, String username) {
        return topicAssociationRepository.findOwningAssociationsByIdsAndUsername(topic.id, getIdsFromAssociatedEntities(associatedTopics), username);
    }


    private List<UUID> getIdsFromAssociatedEntities(List<AssociatedEntityRequest> entities) {
        return entities.stream().map(e -> e.id).toList();
    }
}
