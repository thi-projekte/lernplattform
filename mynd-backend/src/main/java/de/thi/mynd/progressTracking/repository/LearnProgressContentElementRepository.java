package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.progressTracking.entity.LearnProgressContentElement;
import de.thi.mynd.progressTracking.entity.LearnProgressContentElementId;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.entity.LearnProgressTopicId;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class LearnProgressContentElementRepository extends MyndBaseCustomIdRepository<LearnProgressContentElement, LearnProgressContentElementId> {

  public Optional<LearnProgressContentElement> findByContentElementIdAndCreatorId(UUID contentElementId, String creatorId) {
    return find("FROM LearnProgressContentElement t WHERE t.id.contentElementId = ?1 AND t.id.creatorId = ?2", contentElementId, creatorId).singleResultOptional();
  }
}
