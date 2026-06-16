/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "feature_quota_tracking")
@AttributeOverride(
    name = "creatorId",
    column = @Column(name = "creatorId", insertable = false, updatable = false))
public class FeatureQuota extends BaseEntity {

  @EmbeddedId public FeatureQuotaId id;

  @Column public LocalDate dayAccountedFor;

  @Column(nullable = false)
  public int count = 0;
}
