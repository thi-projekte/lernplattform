package de.thi.mynd.topic.repository;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class TopicRepository extends MyndBaseRepository<Topic> {

  public PaginationDto<Topic> findForCreatorPaginated(String creatorId, int page, int pageSize) {
    PanacheQuery<Topic> query = find("creatorId = ?1", creatorId);
    return buildPaginationFromQuery(query, page, pageSize);
  }
}
