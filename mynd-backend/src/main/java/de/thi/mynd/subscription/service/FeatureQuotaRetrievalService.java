/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.LimitsDto;

public interface FeatureQuotaRetrievalService {

  LimitsDto getLimitForFreePlan();

  boolean canLearn(String userId);

  boolean canStartNewTopic(String userId);
}
