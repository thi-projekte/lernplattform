package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class LearnProgressTopicRepository extends MyndBaseCustomIdRepository<LearnProgressTopic, LearnProgressTopicId> {

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
}
