/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public final class TopicRepository extends MyndBaseRepository<Topic> {

  public Optional<Topic> findByTitleOptional(String title) {
    return find("title = ?1", title).singleResultOptional();
  }

  public PaginationDto<Topic> findForCreatorPaginated(String creatorId, int page, int pageSize) {
    PanacheQuery<Topic> query = find("creatorId = ?1", creatorId);
    return buildPaginationFromQuery(query, page, pageSize);
  }

  @SuppressWarnings("unchecked")
  public List<Topic> findBySearch(String search, int limit) {

    return getEntityManager()
        .createNativeQuery(
            "WITH search_query AS (SELECT to_tsquery('german', :search) AS query) "
                + "SELECT topic.* FROM topic, search_query "
                + "WHERE title_search_vector @@ search_query.query "
                + "OR teaser_search_vector @@ search_query.query "
                + "ORDER BY "
                + "ts_rank_cd(title_search_vector, search_query.query) DESC, "
                + "ts_rank_cd(teaser_search_vector, search_query.query) DESC "
                + "LIMIT :limit",
            Topic.class)
        .setParameter("search", safeGermanPrefixSearch(search))
        .setParameter("limit", limit)
        .getResultList();
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

  private String safeGermanPrefixSearch(String search) {
    if (search == null || search.trim().isEmpty()) {
      return "";
    }
    String sanitized = search.replaceAll("[^a-zA-Z0-9äöüÄÖÜß\\s]", " ");

    String tsQuery =
        Arrays.stream(sanitized.trim().split("\\s+"))
            .filter(token -> !token.isEmpty())
            .collect(Collectors.joining(":* & "));

    return tsQuery.isEmpty() ? "" : tsQuery + ":*";
  }
}
