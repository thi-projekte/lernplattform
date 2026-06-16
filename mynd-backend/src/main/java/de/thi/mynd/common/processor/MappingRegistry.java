/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.common.processor;

import de.thi.mynd.common.exception.ObtainNotImplementedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.List;
import java.util.function.Function;

@ApplicationScoped
public final class MappingRegistry {

  @Inject Instance<AbstractMappingProcessor<?, ?>> processors;

  public <E, D> D map(E entity, Class<D> dtoClass) {
    if (entity == null) return null;
    AbstractMappingProcessor<E, D> processor = getMappingProcessor(entity, dtoClass);
    return processor.mapAndEnrich(entity);
  }

  // Map with additional data
  public <E, D> D map(E entity, Class<D> dtoClass, Object... additionalData) {
    if (entity == null) return null;
    AbstractMappingProcessor<E, D> processor = getMappingProcessor(entity, dtoClass);
    return processor.mapAndEnrich(entity, additionalData);
  }

  public <E, D> List<D> mapList(List<E> entities, Class<D> dtoClass) {
    if (entities == null || entities.isEmpty()) return List.of();

    return entities.stream().map(e -> this.map(e, dtoClass)).toList();
  }

  public <E, D> List<D> mapListWithAdditionalData(List<E> entities, Class<D> dtoClass) {
    if (entities == null || entities.isEmpty()) return List.of();

    AbstractMappingProcessor<E, D> processor = getMappingProcessor(entities.getFirst(), dtoClass);
    Object[] additionalData = tryObtainAdditionalData(processor, entities);

    if (additionalData.length > 0) {
      return mapList(entities, dtoClass, additionalData);
    }

    return entities.stream().map(e -> this.map(e, dtoClass)).toList();
  }

  // Map with additional data
  public <E, D> List<D> mapList(List<E> entities, Class<D> dtoClass, Object... additionalData) {
    if (entities == null) return List.of();
    return entities.stream().map(e -> this.map(e, dtoClass, additionalData)).toList();
  }

  public <E, D> List<? extends D> mapList(
      List<E> entities, Function<E, Class<? extends D>> typeResolver) {
    if (entities == null) return List.of();
    return entities.stream().map(e -> this.map(e, typeResolver.apply(e))).toList();
  }

  private <E, D> Object[] tryObtainAdditionalData(
      AbstractMappingProcessor<E, D> processor, List<E> entities) {
    try {
      return processor.obtainAdditionalData(entities);
    } catch (ObtainNotImplementedException e) {
      return new Object[] {};
    }
  }

  @SuppressWarnings("unchecked")
  private <E, D> AbstractMappingProcessor<E, D> getMappingProcessor(E entity, Class<D> dtoClass) {

    return (AbstractMappingProcessor<E, D>)
        processors.stream()
            .filter(p -> p.getEntityType().isInstance(entity) && p.getDtoType().equals(dtoClass))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No processor found for "
                            + entity.getClass().getSimpleName()
                            + " -> "
                            + dtoClass.getSimpleName()));
  }
}
