package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.exception.CategoryAlreadyExistsException;
import de.thi.mynd.topic.exception.CategoryMoveException;
import de.thi.mynd.topic.exception.CategoryNotFoundException;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.request.CategoryRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
  @Transactional
  public void createCategory(CategoryRequest request) {
    if (categoryRepository.existsByTitle(request.title)) {
      throw new CategoryAlreadyExistsException(
          "Category with title '" + request.title + "' already exists");
    }

    Category category = new Category();
    category.title = request.title;
    category.color = request.color;

    categoryRepository.persistAndFlush(category);

    if (request.parentId == null) {
      category.path = category.id.toString().replace("-", "");
    } else {
      Category parent =
          categoryRepository
              .findByIdOptional(request.parentId)
              .orElseThrow(
                  () ->
                      new CategoryNotFoundException(
                          "Parent category not found: " + request.parentId));
      category.path = parent.path + "." + category.id.toString().replace("-", "");
    }
    categoryRepository.persistAndFlush(category);
  }

  @Override
  @Transactional
  public void updateCategory(UUID categoryId, CategoryRequest request) {
    Category category =
        categoryRepository
            .findByIdOptional(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));

    if (!category.title.equals(request.title) && categoryRepository.existsByTitle(request.title)) {
      throw new CategoryAlreadyExistsException(
          "Category with title '" + request.title + "' already exists");
    }

    category.title = request.title;
    category.color = request.color;

    if (request.parentId == null) {
      category.path = category.id.toString().replace("-", "");
    } else {
      Category parent =
          categoryRepository
              .findByIdOptional(request.parentId)
              .orElseThrow(
                  () ->
                      new CategoryNotFoundException(
                          "Parent category not found: " + request.parentId));

      if (parent.path.startsWith(category.path)) {
        throw new CategoryMoveException("Cannot move a category into one of its own descendants");
      }

      String oldPath = category.path;
      String newPath = parent.path + "." + category.id.toString().replace("-", "");
      category.path = newPath;

      categoryRepository.flush();
      categoryRepository.updateDescendantPaths(oldPath, newPath);
    }

    categoryRepository.flush();
  }

  @Override
  public void deleteCategory(UUID categoryId) {
    Category category =
            categoryRepository
                    .findByIdOptional(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));

    category.topics.clear();
    categoryRepository.delete(category);
    categoryRepository.flush();
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
