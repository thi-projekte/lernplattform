package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
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
            .createQuery("SELECT t FROM Topic t WHERE creatorId = :creator ORDER BY t.popularityScore DESC", Topic.class)
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

  public List<Topic> findNMostPopularFilterByCategoryIds(int n, List<UUID> categoryIds, String creatorId) {
    return getSession()
            .createQuery(
                    "SELECT t FROM Topic t LEFT JOIN t.categories c WHERE c.id IN :ids AND t.creatorId = :creator ORDER BY t.popularityScore DESC",
                    Topic.class)
            .setParameter("ids", categoryIds)
            .setParameter("creator", creatorId)
            .setMaxResults(n)
            .getResultList();
  }
}
