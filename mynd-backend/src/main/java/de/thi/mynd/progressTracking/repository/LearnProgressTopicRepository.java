/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class LearnProgressTopicRepository
    extends MyndBaseCustomIdRepository<LearnProgressTopic, LearnProgressTopicId> {

  public Optional<LearnProgressTopic> findOneByTopicIdAndCreatorIdContentElementsFetched(
      UUID topicId, String creatorId) {
    return find(
            "FROM LearnProgressTopic t LEFT JOIN FETCH t.contentElements WHERE t.id.topicId = ?1 AND t.id.creatorId = ?2",
            topicId,
            creatorId)
        .singleResultOptional();
  }

  public List<LearnProgressTopic> findByTopicIdsAndCreatorIdContentElementsFetched(
      List<UUID> topicIds, String creatorId) {
    return find(
            "from LearnProgressTopic t left join fetch t.contentElements "
                + "where t.id.topicId in ?1 and t.id.creatorId = ?2",
            topicIds,
            creatorId)
        .list();
  }

  public List<UUID> getLastNUncompletedTopicIdsForUser(int n, String creatorId) {
    return getSession()
        .createQuery(
            "SELECT t.id.topicId FROM LearnProgressTopic t WHERE t.creatorId = :creatorId AND status IN :allowedStates ORDER BY updatedAt LIMIT :limit",
            UUID.class)
        .setParameter("creatorId", creatorId)
        .setParameter(
            "allowedStates",
            List.of(
                LearnProgressStatus.STARTED))
        .setParameter("limit", n)
        .getResultList();
  }
}
