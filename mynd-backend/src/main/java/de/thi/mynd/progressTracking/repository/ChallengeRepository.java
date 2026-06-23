/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.progressTracking.entity.Challenge;
import de.thi.mynd.progressTracking.entity.ChallengeType;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public final class ChallengeRepository extends MyndBaseRepository<Challenge> {

  public Optional<Challenge> findCurrentForUser(
      String creatorId, ChallengeType type, LocalDate today) {
    return find(
            "creatorId = ?1 AND type = ?2 AND startDate <= ?3 AND endDate >= ?3",
            creatorId,
            type,
            today)
        .firstResultOptional();
  }

  public List<Challenge> findHistoryForUser(String creatorId, LocalDate today, int limit) {
    return find(
            "creatorId = ?1 AND endDate < ?2 ORDER BY endDate DESC LIMIT ?3",
            creatorId,
            today,
            limit)
        .list();
  }
}
