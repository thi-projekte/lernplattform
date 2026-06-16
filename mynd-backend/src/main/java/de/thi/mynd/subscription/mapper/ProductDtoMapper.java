/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.subscription.mapper;

import com.stripe.model.Price;
import com.stripe.model.Product;
import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.subscription.dto.PriceDto;
import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.service.StripeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public final class ProductDtoMapper extends AbstractMappingProcessor<Product, ProductDto> {

  @Inject StripeService stripeService;

  @Override
  public ProductDto mapAndEnrich(Product entity) {

    String tier = entity.getMetadata().get("tier");
    if (tier == null) {
      throw new IllegalArgumentException("The product must have a tier registered");
    }
    SubscriptionStatus status = SubscriptionStatus.valueOf(tier);

    List<Price> prices = stripeService.getAllPricesForProduct(entity.getId());
    List<PriceDto> priceDtos =
        prices.stream()
            .map(
                p ->
                    PriceDto.builder()
                        .id(p.getId())
                        .interval(p.getRecurring().getInterval())
                        .amount((double) p.getUnitAmount() / 100)
                        .build())
            .toList();

    return ProductDto.builder()
        .title(entity.getName())
        .canHaveTrial(false)
        .subscriptionStatus(status)
        .prices(priceDtos)
        .features(stripeService.getProductFeatures(entity.getId()))
        .build();
  }

  @Override
  public ProductDto mapAndEnrich(Product entity, Object... additionalData) {
    ProductDto enriched = super.mapAndEnrich(entity, additionalData);
    enriched.canHaveTrial = !(boolean) additionalData[0];
    return enriched;
  }

  @Override
  public Class<Product> getEntityType() {
    return Product.class;
  }

  @Override
  public Class<ProductDto> getDtoType() {
    return ProductDto.class;
  }
}
