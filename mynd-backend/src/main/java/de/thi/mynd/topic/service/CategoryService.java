package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
import java.util.List;

public interface CategoryService {

  List<CategoryDto> searchMax5(String query);

  List<CategoryDto> findByAssociatedEntities(List<AssociatedEntityRequest> entities);

  // List<CategoryTreeDto> getFullTree();

  // void createCategory(CategoryRequest request);

  // void updateCategory(UUID categoryId, CategoryRequest request);
}
