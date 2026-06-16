/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.service;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.exception.AssociationAlreadyExistsException;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.security.TopicAssociationVoter;
import de.thi.mynd.topic.security.TopicVoter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public final class TopicAssociationServiceImpl implements TopicAssociationService {

  @Inject TopicAssociationRepository topicAssociationRepository;
  @Inject TopicRepository topicRepository;

  @Inject SecurityService securityService;

  @Override
  public List<TopicAssociation> findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
      Topic topic, List<AssociatedEntityRequest> associatedTopics, String username) {
    Log.tracef("Finding and creating topic associations for topicId %s and user %s based in provided topics", topic.id, username);

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

  @Override
  @Transactional
  public TopicAssociation createAssociation(UUID owningTopicId, UUID foreignTopicId) {
    Topic owner = findTopic(owningTopicId);
    Topic foreign = findTopic(foreignTopicId);

    securityService.denyUnlessGranted(owner, TopicVoter.AssignForeignTopics);

    if (topicAssociationRepository.associationExists(owner, foreign)) {
      throw new AssociationAlreadyExistsException("This association already exists");
    }

    TopicAssociation newAssociation = new TopicAssociation();
    newAssociation.owningTopic = owner;
    newAssociation.foreignTopic = foreign;
    topicAssociationRepository.persistAndFlush(newAssociation);

    Log.infof("Successfully created association between %s and %s", owningTopicId, foreignTopicId);

    return newAssociation;
  }

  @Override
  @Transactional
  public void deleteAssociation(UUID associationId) {
    Optional<TopicAssociation> associationOptional =
        topicAssociationRepository.findByIdOptional(associationId);
    if (associationOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("This association does not exist");
    }

    TopicAssociation association = associationOptional.get();
    securityService.denyUnlessGranted(association, TopicAssociationVoter.Delete);

    topicAssociationRepository.delete(association);
    topicAssociationRepository.flush();

    Log.infof("Successfully deleted topic association %s", associationId);
  }

  private Topic findTopic(UUID topicId) {
    Optional<Topic> topicOptional = topicRepository.findByIdOptional(topicId);
    if (topicOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("Topic does not exist");
    }

    return topicOptional.get();
  }

  private List<UUID> getIdsFromAssociatedEntities(List<AssociatedEntityRequest> entities) {
    return entities.stream().map(e -> e.id).toList();
  }
}
