package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.request.CategoryRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

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

  @Override
  public List<CategoryTreeDto> getFullTree() {
    List<Category> sortedCategories = categoryRepository.fetchAllFlat();
    return mapToTree(sortedCategories);
  }

  @Override
  public void createCategory(CategoryRequest request) {

  }

  @Override
  public void updateCategory(UUID categoryId, CategoryRequest request) {

  }

  private List<CategoryTreeDto> mapToTree(List<Category> flat) {
    Map<String, CategoryTreeDto> byPath = new LinkedHashMap<>();
    for (Category category : flat) {
      byPath.put(category.path, CategoryTreeDto.from(category));
    }

    List<CategoryTreeDto> roots = new ArrayList<>();
    for (CategoryTreeDto node : byPath.values()) {
      if (!node.path.contains(".")) {
        roots.add(node);
      } else {
        String parentPath = node.path.substring(0, node.path.lastIndexOf('.'));
        CategoryTreeDto parent = byPath.get(parentPath);
        if (parent != null) {
          parent.children.add(node);
        }
      }
    }

    return roots;
  }
}
