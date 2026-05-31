package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.request.CategoryRequest;
import java.util.List;
import java.util.UUID;

public interface CategoryService {

  List<CategoryDto> searchMax5(String query);

  List<Category> findByAssociatedEntities(List<AssociatedEntityRequest> entities);

  List<CategoryTreeDto> getFullTree();

  void createCategory(CategoryRequest request);

  void updateCategory(UUID categoryId, CategoryRequest request);

  void deleteCategory(UUID categoryId);
}
