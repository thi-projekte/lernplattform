package de.thi.mynd.subscription.mapper;

import com.stripe.model.checkout.Session;
import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.subscription.dto.PaymentSessionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class PaymentSessionDtoMapper
    extends AbstractMappingProcessor<Session, PaymentSessionDto> {
  @Override
  public PaymentSessionDto mapAndEnrich(Session entity) {
    return PaymentSessionDto.builder().url(entity.getUrl()).build();
  }

  @Override
  public Class<Session> getEntityType() {
    return Session.class;
  }

  @Override
  public Class<PaymentSessionDto> getDtoType() {
    return PaymentSessionDto.class;
  }
}
