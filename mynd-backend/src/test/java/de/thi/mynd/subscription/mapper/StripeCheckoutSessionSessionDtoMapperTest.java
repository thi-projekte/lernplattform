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

import com.stripe.model.checkout.Session;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StripeCheckoutSessionSessionDtoMapperTest {

  @Inject StripeCheckoutSessionSessionDtoMapper mapper;

  @Test
  void mapAndEnrich_copiesUrl() {
    Session session = new Session();
    session.setUrl("https://checkout.stripe.com/pay/cs_test_123");

    StripeSessionDto dto = mapper.mapAndEnrich(session);

    assertEquals("https://checkout.stripe.com/pay/cs_test_123", dto.url);
  }

  @Test
  void getEntityType_returnsSession() {
    assertEquals(Session.class, mapper.getEntityType());
  }

  @Test
  void getDtoType_returnsStripeSessionDto() {
    assertEquals(StripeSessionDto.class, mapper.getDtoType());
  }
}
