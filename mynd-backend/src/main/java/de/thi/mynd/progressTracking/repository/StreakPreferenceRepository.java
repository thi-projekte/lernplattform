/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class StreakPreferenceRepository
    extends MyndBaseCustomIdRepository<StreakPreference, CreatorIdKey> {}
