package de.thi.mynd.subscription.dto;

import com.stripe.model.Price;
import lombok.Builder;

import java.util.List;

@Builder
public final class PossiblePricesDto {
    public boolean canHaveTrial;
    public List<Price> prices;
}
