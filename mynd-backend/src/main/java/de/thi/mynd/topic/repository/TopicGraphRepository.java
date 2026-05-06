package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Topic;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicGraphRepository extends MyndBaseRepository<Topic> {

  public List<Topic> findNMostPopular(int n) {
    List<UUID> topIds =
        getSession()
            .createQuery("SELECT t.id FROM Topic t ORDER BY t.popularityScore DESC", UUID.class)
            .setMaxResults(n)
            .getResultList();

    if (topIds.isEmpty()) return Collections.emptyList();

    String hql =
        "SELECT DISTINCT t FROM Topic t "
            + "LEFT JOIN FETCH t.foreignAssociations "
            + "LEFT JOIN FETCH t.ownedAssociations "
            + "WHERE t.id IN (:ids) "
            + "ORDER BY t.popularityScore DESC";

    return getSession().createQuery(hql, Topic.class).setParameter("ids", topIds).getResultList();
  }

  public List<Topic> findNMostPopularFilterByCategoryIds(int n, List<UUID> categoryIds) {
    List<UUID> topIds =
        getSession()
            .createQuery(
                "SELECT t.id FROM Topic t LEFT JOIN t.categories c WHERE c.id IN :ids ORDER BY t.popularityScore DESC",
                UUID.class)
            .setParameter("ids", categoryIds)
            .setMaxResults(n)
            .getResultList();

    if (topIds.isEmpty()) return Collections.emptyList();

    String hql =
        "SELECT DISTINCT t FROM Topic t "
            + "LEFT JOIN FETCH t.foreignAssociations "
            + "LEFT JOIN FETCH t.ownedAssociations "
            + "WHERE t.id IN (:ids) "
            + "ORDER BY t.popularityScore DESC";

    return getSession().createQuery(hql, Topic.class).setParameter("ids", topIds).getResultList();
  }

  public List<Topic> findNeighborsByTopicId(UUID topicId) {
    String hql =
        "SELECT DISTINCT t FROM Topic t "
            + "LEFT JOIN FETCH t.foreignAssociations fa "
            + "LEFT JOIN FETCH t.ownedAssociations oa "
            + "WHERE (fa.id = :id OR oa.id = :id) AND t.id != :id;";

    return getSession().createQuery(hql, Topic.class).setParameter("id", topicId).getResultList();
  }
}
