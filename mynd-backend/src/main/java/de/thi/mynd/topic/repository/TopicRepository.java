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
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class TopicRepository extends MyndBaseRepository<Topic> {

  public Optional<Topic> findByTitleOptional(String title) {
    return find("title = ?1", title).singleResultOptional();
  }

  public PaginationDto<Topic> findForCreatorPaginated(String creatorId, int page, int pageSize) {
    PanacheQuery<Topic> query = find("creatorId = ?1", creatorId);
    return buildPaginationFromQuery(query, page, pageSize);
  }

  public List<Topic> findBySearch(String search, int limit) {
    String formattedSearch = ("%" + search + "%").toLowerCase();
    return find("lower(title) like ?1 or lower(teaser) like ?2", formattedSearch, formattedSearch)
        .range(0, limit)
        .list();
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
