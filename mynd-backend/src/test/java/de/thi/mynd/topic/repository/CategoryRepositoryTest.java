/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.topic.entity.Category;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link CategoryRepository} against a real Postgres instance (Quarkus dev services).
 * Demo content (18 categories) is loaded into the same database at application boot, so every
 * fixture title here is randomized to avoid unique-constraint collisions and false positives from
 * pre-existing rows.
 */
@QuarkusTest
class CategoryRepositoryTest {

  @Inject CategoryRepository categoryRepository;

  private Category newCategory(String title, String path) {
    Category c = new Category();
    c.title = title;
    c.color = "#000000";
    c.path = path;
    c.creatorId = "tester";
    return c;
  }

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  @Test
  @TestTransaction
  void findByTitleOptional_existingTitle_isCaseInsensitiveMatch() {
    String title = unique("Electronics");
    categoryRepository.persistAndFlush(newCategory(title, null));

    Optional<Category> result = categoryRepository.findByTitleOptional(title.toUpperCase());

    assertTrue(result.isPresent());
    assertEquals(title, result.get().title);
  }

  @Test
  @TestTransaction
  void findByTitleOptional_noMatch_returnsEmpty() {
    Optional<Category> result = categoryRepository.findByTitleOptional(unique("DoesNotExist"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByTitleWithLimit_matchesSubstringAndRespectsLimit() {
    String shared = unique("Phones");
    categoryRepository.persistAndFlush(newCategory(shared + "-A", null));
    categoryRepository.persistAndFlush(newCategory(shared + "-B", null));
    categoryRepository.persistAndFlush(newCategory(shared + "-C", null));

    List<Category> result = categoryRepository.findByTitleWithLimit(shared, 2);

    assertEquals(2, result.size());
  }

  @Test
  @TestTransaction
  void findByTitleWithLimit_noMatch_returnsEmptyList() {
    List<Category> result = categoryRepository.findByTitleWithLimit(unique("Nope"), 5);

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void existsByTitle_existingTitle_returnsTrue() {
    String title = unique("Clothing");
    categoryRepository.persistAndFlush(newCategory(title, null));

    assertTrue(categoryRepository.existsByTitle(title));
  }

  @Test
  @TestTransaction
  void existsByTitle_missingTitle_returnsFalse() {
    assertFalse(categoryRepository.existsByTitle(unique("Missing")));
  }

  @Test
  @TestTransaction
  void fetchAllFlat_ordersByPath() {
    String marker = unique("Order");
    Category third = newCategory(marker + "-third", marker + ".3");
    Category first = newCategory(marker + "-first", marker + ".1");
    Category second = newCategory(marker + "-second", marker + ".2");
    categoryRepository.persistAndFlush(third);
    categoryRepository.persistAndFlush(first);
    categoryRepository.persistAndFlush(second);

    List<Category> result =
        categoryRepository.fetchAllFlat().stream()
            .filter(c -> c.path != null && c.path.startsWith(marker))
            .toList();

    assertEquals(3, result.size());
    assertEquals(marker + ".1", result.get(0).path);
    assertEquals(marker + ".2", result.get(1).path);
    assertEquals(marker + ".3", result.get(2).path);
  }

  @Test
  @TestTransaction
  void updateDescendantPaths_rewritesOnlyMatchingDescendants() {
    String rootId = UUID.randomUUID().toString().replace("-", "");
    String childId = UUID.randomUUID().toString().replace("-", "");
    String grandchildId = UUID.randomUUID().toString().replace("-", "");
    String unrelatedId = UUID.randomUUID().toString().replace("-", "");

    String oldRootPath = rootId;
    String childPath = rootId + "." + childId;
    String grandchildPath = rootId + "." + childId + "." + grandchildId;

    categoryRepository.persistAndFlush(newCategory(unique("Root"), oldRootPath));
    Category child = newCategory(unique("Child"), childPath);
    Category grandchild = newCategory(unique("Grandchild"), grandchildPath);
    Category unrelated = newCategory(unique("Unrelated"), unrelatedId);
    categoryRepository.persistAndFlush(child);
    categoryRepository.persistAndFlush(grandchild);
    categoryRepository.persistAndFlush(unrelated);

    String newRootPath = UUID.randomUUID().toString().replace("-", "");
    categoryRepository.updateDescendantPaths(oldRootPath, newRootPath);
    // The native UPDATE bypasses Hibernate's persistence context, so it must be cleared before
    // re-reading the affected rows or findById would return stale cached entities.
    categoryRepository.getEntityManager().clear();

    Category reloadedChild = categoryRepository.findById(child.id);
    Category reloadedGrandchild = categoryRepository.findById(grandchild.id);
    Category reloadedUnrelated = categoryRepository.findById(unrelated.id);

    assertEquals(newRootPath + "." + childId, reloadedChild.path);
    assertEquals(newRootPath + "." + childId + "." + grandchildId, reloadedGrandchild.path);
    assertEquals(unrelatedId, reloadedUnrelated.path);
  }

  @Test
  @TestTransaction
  void findAllWithLimit_respectsLimit() {
    String marker = unique("Limit");
    for (int i = 0; i < 5; i++) {
      categoryRepository.persistAndFlush(newCategory(marker + "-" + i, null));
    }

    List<Category> result = categoryRepository.findAllWithLimit(3);

    assertEquals(3, result.size());
  }

  @Test
  @TestTransaction
  void findByIdsTypeSafe_returnsOnlyRequestedIds() {
    Category a = newCategory(unique("A"), null);
    Category b = newCategory(unique("B"), null);
    Category c = newCategory(unique("C"), null);
    categoryRepository.persistAndFlush(a);
    categoryRepository.persistAndFlush(b);
    categoryRepository.persistAndFlush(c);

    List<Category> result = categoryRepository.findByIdsTypeSafe(List.of(a.id, c.id));

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(cat -> cat.id.equals(a.id)));
    assertTrue(result.stream().anyMatch(cat -> cat.id.equals(c.id)));
    assertTrue(result.stream().noneMatch(cat -> cat.id.equals(b.id)));
  }

  @Test
  @TestTransaction
  void findByIdsTypeSafe_emptyList_returnsEmpty() {
    List<Category> result = categoryRepository.findByIdsTypeSafe(List.of());

    assertTrue(result.isEmpty());
  }
}
