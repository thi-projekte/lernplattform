package de.thi.mynd.topic.repository;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicRepository extends MyndBaseRepository<Topic> {

  public PaginationDto<Topic> findForCreatorPaginated(String creatorId, int page, int pageSize) {
    PanacheQuery<Topic> query = find("creatorId = ?1", creatorId);
    return buildPaginationFromQuery(query, page, pageSize);
  }

  @SuppressWarnings("unchecked")
  public List<Topic> findBySearch(String search, int limit) {
    return getEntityManager()
            .createNativeQuery(
                    "SELECT * FROM topic WHERE "
                            + "title_search_vector @@ plainto_tsquery('german', :search) OR "
                            + "teaser_search_vector @@ plainto_tsquery('german', :search) "
                    "WITH search_query AS (SELECT plainto_tsquery('german', :search) AS query) "
                            + "SELECT topic.* FROM topic, search_query "
                            + "WHERE title_search_vector @@ search_query.query "
                            + "OR teaser_search_vector @@ search_query.query "
                            + "ORDER BY "
                            + "ts_rank_cd(title_search_vector, search_query.query) DESC, "
                            + "ts_rank_cd(teaser_search_vector, search_query.query) DESC "
                            + "LIMIT :limit",
                    Topic.class)
            .setParameter("search", search)
            .setParameter("limit", limit)
            .getResultList();
  }

  @jakarta.transaction.Transactional
  public void updateSearchVectors(java.util.UUID topicId, String title, String teaser) {
    getEntityManager()
            .createNativeQuery(
                    "UPDATE topic SET "
                            + "title_search_vector  = to_tsvector('german', :title), "
                            + "teaser_search_vector = to_tsvector('german', :teaser) "
                            + "WHERE id = :id")
            .setParameter("title", title)
            .setParameter("teaser", teaser)
            .setParameter("id", topicId)
            .executeUpdate();
  }

  public List<Topic> findByOwningTopicId(UUID topicId) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Topic> cq = cb.createQuery(Topic.class);
    Root<Topic> root = cq.from(Topic.class);
    Join<Topic, TopicAssociation> firstJoin = root.join("foreignAssociations");
    Join<TopicAssociation, Topic> secondJoin = firstJoin.join("owningTopic");
    cq.where(cb.equal(secondJoin.get("id"), topicId));

    return getEntityManager().createQuery(cq).getResultList();
  }
}