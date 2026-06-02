package de.thi.mynd.subscription.dto;

import de.thi.mynd.subscription.entity.SubscriptionStatus;
import java.util.List;
import lombok.Builder;

@Builder
public final class ProductDto {
  public String title;
  public SubscriptionStatus subscriptionStatus;
  public List<PossiblePricesDto> prices;
}
