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

  /**
   * Maps a single entity to the given DTO class using the matching {@link
   * AbstractMappingProcessor}.
   *
   * @param <E> the entity type
   * @param <D> the DTO type
   * @param entity the entity to map; returns {@code null} if {@code null} is passed
   * @param dtoClass the target DTO class
   * @return the mapped DTO, or {@code null} if the entity was {@code null}
   */
  public <E, D> D map(E entity, Class<D> dtoClass) {
    if (entity == null) return null;
    AbstractMappingProcessor<E, D> processor = getMappingProcessor(entity, dtoClass);
    return processor.mapAndEnrich(entity);
  }

  /**
   * Maps a single entity to the given DTO class, passing additional data to the processor. Use this
   * overload when the processor requires contextual data that cannot be derived from the entity
   * alone (e.g. pre-fetched aggregates or security context values).
   *
   * @param <E> the entity type
   * @param <D> the DTO type
   * @param entity the entity to map; returns {@code null} if {@code null} is passed
   * @param dtoClass the target DTO class
   * @param additionalData arbitrary extra data forwarded to {@link
   *     AbstractMappingProcessor#mapAndEnrich}
   * @return the mapped DTO, or {@code null} if the entity was {@code null}
   */
  public <E, D> D map(E entity, Class<D> dtoClass, Object... additionalData) {
    if (entity == null) return null;
    AbstractMappingProcessor<E, D> processor = getMappingProcessor(entity, dtoClass);
    return processor.mapAndEnrich(entity, additionalData);
  }

  /**
   * Maps a list of entities to the given DTO class. If the resolved processor implements {@link
   * AbstractMappingProcessor#obtainAdditionalData}, that data is fetched once for the entire list
   * and forwarded to each individual mapping call, avoiding per-element lookups (e.g. bulk DB
   * queries). NOTE: If you pass a generic entity, the first match from your entity list will be the
   * processor to obtain the additional data from
   *
   * @param <E> the entity type
   * @param <D> the DTO type
   * @param entities the entities to map; returns an empty list if {@code null} or empty
   * @param dtoClass the target DTO class
   * @return an immutable list of mapped DTOs
   */
  public <E, D> List<D> mapList(List<E> entities, Class<D> dtoClass) {
    if (entities == null || entities.isEmpty()) return List.of();

    AbstractMappingProcessor<E, D> processor = getMappingProcessor(entities.getFirst(), dtoClass);
    Object[] additionalData = tryObtainAdditionalData(processor, entities);

    if (additionalData.length > 0) {
      return mapList(entities, dtoClass, additionalData);
    }

    return entities.stream().map(e -> this.map(e, dtoClass)).toList();
  }

  /**
   * Maps a list of entities to the given DTO class, passing additional data to every individual
   * mapping call.
   *
   * @param <E> the entity type
   * @param <D> the DTO type
   * @param entities the entities to map; returns an empty list if {@code null}
   * @param dtoClass the target DTO class
   * @param additionalData arbitrary extra data forwarded to each {@link
   *     AbstractMappingProcessor#mapAndEnrich} call
   * @return an immutable list of mapped DTOs
   */
  public <E, D> List<D> mapList(List<E> entities, Class<D> dtoClass, Object... additionalData) {
    if (entities == null) return List.of();
    return entities.stream().map(e -> this.map(e, dtoClass, additionalData)).toList();
  }

  /**
   * Maps a heterogeneous list of entities where each element may resolve to a different DTO
   * subtype. The concrete DTO class is determined per element via the provided {@code typeResolver}
   * function, allowing polymorphic mapping within a single list.
   *
   * @param <E> the common entity base type
   * @param <D> the common DTO base type
   * @param entities the entities to map; returns an empty list if {@code null}
   * @param typeResolver a function that returns the concrete DTO class for a given entity
   * @return an immutable list of mapped DTOs, each potentially of a different subtype of {@code D}
   */
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
