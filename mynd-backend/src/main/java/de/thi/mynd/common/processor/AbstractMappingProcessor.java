package de.thi.mynd.common.processor;

import de.thi.mynd.common.exception.ObtainNotImplementedException;
import de.thi.mynd.common.service.IdentityService;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Base class for all mapping processors responsible for converting an entity of type {@code E} to a
 * DTO of type {@code D}. Processors are discovered and invoked by {@link MappingRegistry}.
 *
 * <p>Subclasses must implement {@link #mapAndEnrich(Object)} for basic mapping. Optionally, they
 * can override {@link #obtainAdditionalData(List)} to enable efficient bulk data fetching and
 * {@link #mapAndEnrich(Object, Object...)} to consume that data during mapping.
 *
 * @param <E> the entity type this processor handles
 * @param <D> the DTO type this processor produces
 */
public abstract class AbstractMappingProcessor<E, D> {

  @Inject protected IdentityService identityService;

  /**
   * Maps the given entity to its corresponding DTO, enriching it with any additional data required
   * (e.g. from the security context or related entities).
   *
   * @param entity the entity to map; must not be {@code null}
   * @return the mapped and enriched DTO
   */
  public abstract D mapAndEnrich(E entity);

  /**
   * Returns the entity class this processor handles. Used by {@link MappingRegistry} to match
   * processors to incoming entities at runtime.
   *
   * @return the entity type
   */
  public abstract Class<E> getEntityType();

  /**
   * Returns the DTO class this processor produces. Used by {@link MappingRegistry} to match
   * processors to the requested target type.
   *
   * @return the DTO type
   */
  public abstract Class<D> getDtoType();

  /**
   * Maps the given entity to its DTO using the provided additional data. The default implementation
   * ignores {@code additionalData} and delegates to {@link #mapAndEnrich(Object)}. Override this
   * when the processor requires contextual data that was pre-fetched via {@link
   * #obtainAdditionalData(List)}.
   *
   * @param entity the entity to map; must not be {@code null}
   * @param additionalData arbitrary data forwarded from the {@link MappingRegistry}, typically the
   *     result of {@link #obtainAdditionalData(List)}
   * @return the mapped and enriched DTO
   */
  public D mapAndEnrich(E entity, Object... additionalData) {
    return mapAndEnrich(entity);
  }

  /**
   * Fetches additional data required to map the given list of entities, allowing bulk lookups (e.g.
   * a single DB query) instead of per-entity fetches. The returned array is passed as {@code
   * additionalData} to each {@link #mapAndEnrich(Object, Object...)} call.
   *
   * <p>The default implementation throws {@link ObtainNotImplementedException}, signalling to
   * {@link MappingRegistry} that no bulk prefetching is needed for this processor. Override this
   * method when mapping a list would otherwise cause N+1 queries.
   *
   * @param entities the full list of entities about to be mapped
   * @return an array of additional data objects to be forwarded to each mapping call
   * @throws ObtainNotImplementedException if bulk prefetching is not supported by this processor
   */
  public Object[] obtainAdditionalData(List<E> entities) {
    throw new ObtainNotImplementedException();
  }
}
