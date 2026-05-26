package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Category;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public final class CategoryRepository extends MyndBaseRepository<Category> {

  @Inject
  EntityManager em;

  public Optional<Category> findByTitleOptional(String title) {
    return find("title = ?1", title).singleResultOptional();
  }

  public List<Category> findByTitleWithLimit(String title, int limit) {
    return find("lower(title) like ?1", "%" + title.toLowerCase() + "%").range(0, limit).list();
  }

  public List<Category> fetchAllFlat() {
    return list("ORDER BY path");
  }
}
