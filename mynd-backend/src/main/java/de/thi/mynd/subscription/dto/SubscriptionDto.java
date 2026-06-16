/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.subscription.dto;

import de.thi.mynd.subscription.entity.SubscriptionStatus;
import lombok.Builder;

@Builder
public final class SubscriptionDto {
  public String creatorId;
  public SubscriptionStatus subscriptionStatus;
  public boolean canAccessBillingPortal;
  public boolean canLearnTopics;
  public boolean canStartNewTopics;
}
