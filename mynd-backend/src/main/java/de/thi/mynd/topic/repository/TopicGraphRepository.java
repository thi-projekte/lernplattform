package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.topic.entity.Topic;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicGraphRepository extends MyndBaseRepository<Topic> {

  public List<Topic> findNMostPopular(int n) {

    return getSession()
        .createQuery("SELECT t FROM Topic t ORDER BY t.popularityScore DESC", Topic.class)
        .setMaxResults(n)
        .getResultList();
  }

  public List<Topic> findNMostPopular(int n, String creatorId) {

    return getSession()
        .createQuery(
            "SELECT t FROM Topic t WHERE creatorId = :creator ORDER BY t.popularityScore DESC",
            Topic.class)
        .setParameter("creator", creatorId)
        .setMaxResults(n)
        .getResultList();
  }

  public List<Topic> findNMostPopularFilterByCategoryIds(int n, List<UUID> categoryIds) {
    return getSession()
        .createQuery(
            "SELECT t FROM Topic t LEFT JOIN t.categories c WHERE c.id IN :ids ORDER BY t.popularityScore DESC",
            Topic.class)
        .setParameter("ids", categoryIds)
        .setMaxResults(n)
        .getResultList();
  }

  public List<Topic> findNMostPopularFilterByCategoryIds(
      int n, List<UUID> categoryIds, String creatorId) {
    return getSession()
        .createQuery(
            "SELECT t FROM Topic t LEFT JOIN t.categories c WHERE c.id IN :ids AND t.creatorId = :creator ORDER BY t.popularityScore DESC",
            Topic.class)
        .setParameter("ids", categoryIds)
        .setParameter("creator", creatorId)
        .setMaxResults(n)
        .getResultList();
  }

  public List<Topic> findForAssociatedTopicsNotStartedAllCompleted(List<UUID> seedIds, String creatorId) {

    String[] allowedStatus = {
            LearnProgressStatus.COMPLETED_MANUALLY.name(),
            LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED.name()
    };

    return getEntityManager().createNativeQuery("""
                WITH RECURSIVE topic_tree AS (
                    SELECT
                        ta.foreign_topic_id AS topic_id,
                        ARRAY[ta.owning_topic_id] AS visited,
                        1 AS depth
                    FROM topic_association ta
                    WHERE ta.owning_topic_id = ANY(:seedIds)

                    UNION

                    SELECT
                        ta.foreign_topic_id,
                        tt.visited || ta.owning_topic_id,
                        tt.depth + 1
                    FROM topic_association ta
                    INNER JOIN topic_tree tt
                        ON ta.owning_topic_id = tt.topic_id
                       AND ta.foreign_topic_id != ALL(tt.visited)
                )
                SELECT DISTINCT t.*
                FROM topic_tree tt
                INNER JOIN topic t
                    ON t.id = tt.topic_id
                LEFT JOIN learn_progress_topic lpt
                    ON lpt.topicId = t.id
                    AND lpt.creatorId = :creatorId
                WHERE lpt.status = ANY(CAST(:allowedStatus AS varchar[]))
                """, Topic.class)
            .setParameter("seedIds", seedIds.toArray(UUID[]::new))
            .setParameter("creatorId", creatorId)
            .setParameter("allowedStatus", allowedStatus)
            .getResultList();
  }
}
