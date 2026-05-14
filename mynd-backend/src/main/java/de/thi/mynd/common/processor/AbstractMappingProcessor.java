package de.thi.mynd.common.processor;

import de.thi.mynd.common.exception.ObtainNotImplementedException;
import de.thi.mynd.common.service.IdentityService;
import jakarta.inject.Inject;

import java.util.List;

public abstract class AbstractMappingProcessor<E, D> {

  @Inject protected IdentityService identityService;

  public abstract D mapAndEnrich(E entity);

  public abstract Class<E> getEntityType();

  public abstract Class<D> getDtoType();

  public D mapAndEnrich(E entity, Object... additionalData) {
      return mapAndEnrich(entity);
  }

  public Object[] obtainAdditionalData(List<E> entities) {
    throw new ObtainNotImplementedException();
  }
}
