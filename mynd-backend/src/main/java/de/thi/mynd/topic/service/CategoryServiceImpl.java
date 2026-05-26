package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class CategoryServiceImpl implements CategoryService {

  @Inject CategoryRepository categoryRepository;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public List<CategoryDto> searchMax5(String query) {
    if (query == null) {
      return mappingRegistry.mapList(categoryRepository.findAllWithLimit(5), CategoryDto.class);
    }
    return mappingRegistry.mapList(
        categoryRepository.findByTitleWithLimit(query, 5), CategoryDto.class);
  }

  @Override
  public List<Category> findByAssociatedEntities(List<AssociatedEntityRequest> entities) {
    List<UUID> ids = entities.stream().map((e) -> e.id).toList();
    return categoryRepository.findByIdsTypeSafe(ids);
  }
}
