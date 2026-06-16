/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.subscription.dto;

import com.stripe.model.ProductFeature;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import java.util.List;
import lombok.Builder;

@Builder
public final class ProductDto {
  public String title;
  public SubscriptionStatus subscriptionStatus;
  public boolean canHaveTrial;
  public List<PriceDto> prices;
  public List<ProductFeature> features;
}
