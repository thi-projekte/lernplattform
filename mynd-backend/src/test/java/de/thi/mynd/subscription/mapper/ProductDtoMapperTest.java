/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.ProductFeature;
import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.service.StripeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductDtoMapperTest {

  @Inject ProductDtoMapper productDtoMapper;

  @InjectMock StripeService stripeService;

  private Product product(String tier) {
    Product product = new Product();
    product.setId("prod_123");
    product.setName("Premium Plan");
    Map<String, String> metadata = new HashMap<>();
    if (tier != null) {
      metadata.put("tier", tier);
    }
    product.setMetadata(metadata);
    return product;
  }

  private Price price(String id, String interval, long unitAmount) {
    Price price = new Price();
    price.setId(id);
    price.setUnitAmount(unitAmount);
    Price.Recurring recurring = new Price.Recurring();
    recurring.setInterval(interval);
    price.setRecurring(recurring);
    return price;
  }

  @Test
  void mapAndEnrich_missingTierMetadata_throwsIllegalArgumentException() {
    Product product = product(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> productDtoMapper.mapAndEnrich(product));

    assertEquals("The product must have a tier registered", ex.getMessage());
  }

  @Test
  void mapAndEnrich_invalidTierValue_throwsIllegalArgumentExceptionFromEnum() {
    Product product = product("NOT_A_REAL_TIER");
    when(stripeService.getAllPricesForProduct("prod_123")).thenReturn(List.of());

    assertThrows(IllegalArgumentException.class, () -> productDtoMapper.mapAndEnrich(product));
  }

  @Test
  void mapAndEnrich_emptyPriceList_resultsInEmptyPrices() {
    Product product = product("FREE");
    when(stripeService.getAllPricesForProduct("prod_123")).thenReturn(List.of());
    when(stripeService.getProductFeatures("prod_123")).thenReturn(List.of());

    ProductDto dto = productDtoMapper.mapAndEnrich(product);

    assertEquals("Premium Plan", dto.title);
    assertEquals(SubscriptionStatus.FREE, dto.subscriptionStatus);
    assertTrue(dto.prices.isEmpty());
  }

  @Test
  void mapAndEnrich_multiplePrices_mapsAmountAndIntervalForEach() {
    Product product = product("PREMIUM");
    Price monthly = price("price_month", "month", 1999);
    Price yearly = price("price_year", "year", 19999);
    when(stripeService.getAllPricesForProduct("prod_123")).thenReturn(List.of(monthly, yearly));
    when(stripeService.getProductFeatures("prod_123"))
        .thenReturn(List.of(mock(ProductFeature.class)));

    ProductDto dto = productDtoMapper.mapAndEnrich(product);

    assertEquals(2, dto.prices.size());
    assertEquals("price_month", dto.prices.get(0).id);
    assertEquals("month", dto.prices.get(0).interval);
    assertEquals(19.99, dto.prices.get(0).amount, 0.0001);
    assertEquals("price_year", dto.prices.get(1).id);
    assertEquals("year", dto.prices.get(1).interval);
    assertEquals(199.99, dto.prices.get(1).amount, 0.0001);
    assertEquals(1, dto.features.size());
  }

  @Test
  void mapAndEnrich_singleArgOverload_canHaveTrialDefaultsFalse() {
    Product product = product("FREE");
    when(stripeService.getAllPricesForProduct("prod_123")).thenReturn(List.of());
    when(stripeService.getProductFeatures("prod_123")).thenReturn(List.of());

    ProductDto dto = productDtoMapper.mapAndEnrich(product);

    assertFalse(dto.canHaveTrial);
  }

  @Test
  void mapAndEnrichWithAdditionalData_usedTrialTrue_canHaveTrialIsFalse() {
    Product product = product("FREE");
    when(stripeService.getAllPricesForProduct("prod_123")).thenReturn(List.of());
    when(stripeService.getProductFeatures("prod_123")).thenReturn(List.of());

    ProductDto dto = productDtoMapper.mapAndEnrich(product, (Object) Boolean.TRUE);

    assertFalse(dto.canHaveTrial);
  }

  @Test
  void mapAndEnrichWithAdditionalData_usedTrialFalse_canHaveTrialIsTrue() {
    Product product = product("FREE");
    when(stripeService.getAllPricesForProduct("prod_123")).thenReturn(List.of());
    when(stripeService.getProductFeatures("prod_123")).thenReturn(List.of());

    ProductDto dto = productDtoMapper.mapAndEnrich(product, (Object) Boolean.FALSE);

    assertTrue(dto.canHaveTrial);
  }

  @Test
  void getEntityType_returnsProduct() {
    assertEquals(Product.class, productDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsProductDto() {
    assertEquals(ProductDto.class, productDtoMapper.getDtoType());
  }
}
