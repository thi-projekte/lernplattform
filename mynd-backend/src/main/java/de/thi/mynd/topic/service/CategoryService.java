package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Category;
import java.util.List;

public interface CategoryService {

  List<Category> searchMax5(String query);

  List<Category> findByAssociatedEntities(List<AssociatedEntityRequest> entities);
}
