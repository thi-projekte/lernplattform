package de.thi.mynd.subscription.dto;

import de.thi.mynd.subscription.entity.SubscriptionStatus;
import lombok.Builder;

@Builder
public final class SubscriptionDto {
    public String creatorId;
    public SubscriptionStatus subscriptionStatus;
}
