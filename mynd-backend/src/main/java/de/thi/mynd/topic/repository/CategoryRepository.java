/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
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

  @Inject EntityManager em;

  public Optional<Category> findByTitleOptional(String title) {
    return find("lower(title) = ?1", title.toLowerCase()).singleResultOptional();
  }

  public List<Category> findByTitleWithLimit(String title, int limit) {
    return find("lower(title) like ?1", "%" + title.toLowerCase() + "%").range(0, limit).list();
  }

  public List<Category> fetchAllFlat() {
    return list("ORDER BY path");
  }

  public boolean existsByTitle(String title) {
    return find("title = ?1", title).count() > 0;
  }

  public void updateDescendantPaths(String oldPath, String newPath) {
    em.createNativeQuery(
            """
            UPDATE category
            SET path = CAST(:newPath AS ltree) || subpath(path, nlevel(CAST(:oldPath AS ltree)))
            WHERE path <@ CAST(:oldPath AS ltree)
            AND path != CAST(:oldPath AS ltree)
            """)
        .setParameter("oldPath", oldPath)
        .setParameter("newPath", newPath)
        .executeUpdate();
  }
}
