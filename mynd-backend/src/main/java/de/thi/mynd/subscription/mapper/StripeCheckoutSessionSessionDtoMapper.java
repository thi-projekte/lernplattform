/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.subscription.mapper;

import com.stripe.model.checkout.Session;
import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.subscription.dto.StripeSessionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class StripeCheckoutSessionSessionDtoMapper
    extends AbstractMappingProcessor<Session, StripeSessionDto> {
  @Override
  public StripeSessionDto mapAndEnrich(Session entity) {
    return StripeSessionDto.builder().url(entity.getUrl()).build();
  }

  @Override
  public Class<Session> getEntityType() {
    return Session.class;
  }

  @Override
  public Class<StripeSessionDto> getDtoType() {
    return StripeSessionDto.class;
  }
}
