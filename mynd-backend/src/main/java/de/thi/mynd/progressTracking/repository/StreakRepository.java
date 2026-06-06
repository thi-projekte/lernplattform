package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.progressTracking.entity.Streak;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public final class StreakRepository extends MyndBaseRepository<Streak> {

  public List<Streak> findNotEndedByCreatorId(String creatorId) {
    return find("endedAt IS NULL AND creatorId = ?1", creatorId).list();
  }
}
