/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.entity.StreakType;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public final class StreakRepository extends MyndBaseRepository<Streak> {

  public List<Streak> findNotEndedByCreatorId(String creatorId) {
    return find("endedAt IS NULL AND creatorId = ?1", creatorId).list();
  }

  public Optional<Streak> findNotEndedByCreatorIdAndType(String creatorId, StreakType type) {
    return find("endedAt IS NULL AND creatorId = ?1 AND type = ?2", creatorId, type)
        .firstResultOptional();
  }
}
