package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import de.thi.mynd.subscription.entity.SubscriptionStatus;

import java.util.List;

public interface PaymentService {

  StripeSessionDto createInitialSubscriptionSession(String priceId);

  List<ProductDto> getAllProducts();
}
