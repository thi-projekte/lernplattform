package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import java.util.List;

public interface PaymentService {

  StripeSessionDto createInitialSubscriptionSession(String priceId);

  List<ProductDto> getAllProducts();

  void createTrial(String priceId);
}
