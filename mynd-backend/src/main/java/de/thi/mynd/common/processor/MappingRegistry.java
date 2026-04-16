package de.thi.mynd.common.processor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ApplicationScoped
public final class MappingRegistry {

  @Inject Instance<AbstractMappingProcessor<?, ?>> processors;

  @SuppressWarnings("unchecked")
  public <E, D> D map(E entity, Class<D> dtoClass) {

    if (entity == null) return null;

    AbstractMappingProcessor<E, D> processor =
        (AbstractMappingProcessor<E, D>)
            processors.stream()
                .filter(
                    p -> p.getEntityType().isInstance(entity) && p.getDtoType().equals(dtoClass))
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "No processor found for "
                                + entity.getClass().getSimpleName()
                                + " -> "
                                + dtoClass.getSimpleName()));

    return processor.mapAndEnrich(entity);
  }

  public <E, D> List<D> mapList(List<E> entities, Class<D> dtoClass) {
    if (entities == null) return List.of();

    return entities.stream().map(e -> this.map(e, dtoClass)).toList();
  }

  public <E, D> List<? extends D> mapList(List<E> entities, Function<E, Class<? extends D>> typeResolver) {
    if (entities == null) return List.of();

    return entities.stream()
            .map(e -> this.map(e, typeResolver.apply(e)))
            .toList();
  }
}
