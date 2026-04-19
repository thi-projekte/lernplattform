package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoryServiceImpl implements CategoryService {

  @Inject CategoryRepository categoryRepository;

  @Override
  public List<Category> search(String query) {
    if (query == null) {
      return categoryRepository.findAllWithLimit(5);
    }
    return categoryRepository.findByTitleWithLimit(query, 5);
  }

  @Override
  public List<Category> findByAssociatedEntities(List<AssociatedEntityRequest> entities) {
    List<UUID> ids = entities.stream()
            .map((e) -> e.id)
            .toList();
    return categoryRepository.findByIdsTypeSafe(ids);
  }
}
