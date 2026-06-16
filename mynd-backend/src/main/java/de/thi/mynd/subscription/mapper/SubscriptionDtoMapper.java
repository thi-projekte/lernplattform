/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.service.FeatureQuotaRetrievalService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class SubscriptionDtoMapper
    extends AbstractMappingProcessor<Subscription, SubscriptionDto> {

  @Inject FeatureQuotaRetrievalService featureQuotaRetrievalService;

  @Override
  public SubscriptionDto mapAndEnrich(Subscription entity) {
    return SubscriptionDto.builder()
        .creatorId(entity.creatorId)
        .subscriptionStatus(entity.subscriptionStatus)
        .canAccessBillingPortal(entity.stripeCustomerId != null)
        .canLearnTopics(featureQuotaRetrievalService.canLearn(entity.id.creatorId))
        .canStartNewTopics(featureQuotaRetrievalService.canStartNewTopic(entity.id.creatorId))
        .build();
  }

  @Override
  public Class<Subscription> getEntityType() {
    return Subscription.class;
  }

  @Override
  public Class<SubscriptionDto> getDtoType() {
    return SubscriptionDto.class;
  }
}
