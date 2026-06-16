/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.service;

import de.thi.mynd.subscription.dto.ProductDto;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import java.util.List;

public interface PaymentService {

  StripeSessionDto createInitialSubscriptionSession(String priceId);

  List<ProductDto> getAllProducts();

  void createTrial(String priceId);
}
