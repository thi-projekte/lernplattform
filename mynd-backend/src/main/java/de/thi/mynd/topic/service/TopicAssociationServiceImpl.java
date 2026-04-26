package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public final class TopicAssociationServiceImpl implements TopicAssociationService {

  @Inject TopicAssociationRepository topicAssociationRepository;

  @Override
  public List<TopicAssociation> findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
      Topic topic, List<AssociatedEntityRequest> associatedTopics, String username) {

    List<UUID> desiredTopics = getIdsFromAssociatedEntities(associatedTopics);

    List<TopicAssociation> existingAssociations =
        topicAssociationRepository.findOwningAssociationsByIdsAndUsername(
            topic.id, desiredTopics, username);

    Set<UUID> existingIds =
        existingAssociations.stream().map(a -> a.foreignTopic.id).collect(Collectors.toSet());

    List<UUID> idsToCreate =
        desiredTopics.stream().filter(id -> !existingIds.contains(id)).toList();

    for (UUID idToCreate : idsToCreate) {
      TopicAssociation newAssociation = new TopicAssociation();
      newAssociation.creatorId = username;
      newAssociation.owningTopic = topic;
      newAssociation.foreignTopic =
          topicAssociationRepository.getEntityManager().getReference(Topic.class, idToCreate);
      existingAssociations.add(newAssociation);
      topicAssociationRepository.persist(newAssociation);
    }

    return existingAssociations;
  }

  private List<UUID> getIdsFromAssociatedEntities(List<AssociatedEntityRequest> entities) {
    return entities.stream().map(e -> e.id).toList();
  }
}
