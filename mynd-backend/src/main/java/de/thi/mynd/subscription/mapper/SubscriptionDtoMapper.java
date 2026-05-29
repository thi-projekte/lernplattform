package de.thi.mynd.subscription.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.subscription.dto.SubscriptionDto;
import de.thi.mynd.subscription.entity.Subscription;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class SubscriptionDtoMapper extends AbstractMappingProcessor<Subscription, SubscriptionDto> {
    @Override
    public SubscriptionDto mapAndEnrich(Subscription entity) {
        return SubscriptionDto.builder()
                .creatorId(entity.creatorId)
                .subscriptionStatus(entity.subscriptionStatus)
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
