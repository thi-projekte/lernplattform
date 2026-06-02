package de.thi.mynd.subscription.dto;

import de.thi.mynd.subscription.entity.SubscriptionStatus;
import lombok.Builder;

import java.util.List;

@Builder
public final class ProductDto {
    public String title;
    public SubscriptionStatus subscriptionStatus;
    public List<PossiblePricesDto> prices;
}
