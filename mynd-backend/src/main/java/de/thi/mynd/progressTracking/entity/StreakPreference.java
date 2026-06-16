/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

@Entity
@Table(name = "streak_preference")
public class StreakPreference extends BaseEntityWithCreatorIdPk {

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public StreakType type;

  @Column(nullable = false)
  public boolean isPublic;
}
