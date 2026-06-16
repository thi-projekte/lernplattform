/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.repository;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public final class FeatureQuotaRepository
    extends MyndBaseCustomIdRepository<FeatureQuota, CreatorIdKey> {

  public Optional<FeatureQuota> findByCreatorAndFeatureAndDate(
      String creatorId, Feature feature, LocalDate date) {
    return find(
            "id.creatorId = ?1 AND id.feature = ?2 AND dayAccountedFor = ?3",
            creatorId,
            feature,
            date)
        .firstResultOptional();
  }

  public Optional<FeatureQuota> findByCreatorAndFeature(String creatorId, Feature feature) {
    return find("id.creatorId = ?1 AND id.feature = ?2", creatorId, feature).firstResultOptional();
  }
}
