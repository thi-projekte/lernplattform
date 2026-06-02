package de.thi.mynd.subscription.dto;

import com.stripe.model.Price;
import java.util.List;
import lombok.Builder;

@Builder
public final class PossiblePricesDto {
  public boolean canHaveTrial;
  public List<Price> prices;
}
