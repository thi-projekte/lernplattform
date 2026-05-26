package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CategoryServiceImplTest {

  @Inject CategoryServiceImpl categoryService;

  @InjectMock CategoryRepository categoryRepository;

  @Test
  void testSearch_Max5_WithNullQuery() {
    // Arrange
    Category cat = new Category();
    cat.title = "Default Category";
    when(categoryRepository.findAllWithLimit(5)).thenReturn(List.of(cat));

    // Act
    List<CategoryDto> results = categoryService.searchMax5(null);

    // Assert
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals("Default Category", results.get(0).title);
    verify(categoryRepository).findAllWithLimit(5);
    verify(categoryRepository, never()).findByTitleWithLimit(anyString(), anyInt());
  }

  @Test
  void testSearch_Max5_WithQuery() {
    // Arrange
    String query = "Java";
    when(categoryRepository.findByTitleWithLimit(eq(query), eq(5)))
        .thenReturn(List.of(new Category()));

    // Act
    categoryService.searchMax5(query);

    // Assert
    verify(categoryRepository).findByTitleWithLimit(query, 5);
    verify(categoryRepository, never()).findAllWithLimit(anyInt());
  }

  @Test
  void testFindByAssociatedEntities() {
    // Arrange
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    AssociatedEntityRequest req1 = new AssociatedEntityRequest();
    req1.id = id1;
    AssociatedEntityRequest req2 = new AssociatedEntityRequest();
    req2.id = id2;

    List<AssociatedEntityRequest> requests = List.of(req1, req2);

    when(categoryRepository.findByIdsTypeSafe(anyList()))
        .thenReturn(List.of(new Category(), new Category()));

    // Act
    List<Category> results = categoryService.findByAssociatedEntities(requests);

    // Assert
    Assertions.assertEquals(2, results.size());
    verify(categoryRepository)
        .findByIdsTypeSafe(
            argThat(list -> list.contains(id1) && list.contains(id2) && list.size() == 2));
  }

  // ── Helpers ─────────────────────────────────────────────────────────────

  private Category cat(UUID id, String name, String path) {
    Category c = new Category();
    c.id = id;
    c.title = name;
    c.path = path;
    return c;
  }

  // ── Tests ────────────────────────────────────────────────────────────────

  @Test
  void getFullTree_emptyDatabase_returnsEmptyList() {
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of());

    List<CategoryTreeDto> result = categoryService.getFullTree();

    assertTrue(result.isEmpty());
  }

  @Test
  void getFullTree_singleRootNode_returnsSingleRoot() {
    UUID cat1 = UUID.randomUUID();
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of(
            cat(cat1, "Electronics", "1")
    ));

    List<CategoryTreeDto> result = categoryService.getFullTree();

    assertEquals(1, result.size());
    assertEquals("Electronics", result.get(0).title);
    assertTrue(result.get(0).children.isEmpty());
  }

  @Test
  void getFullTree_multipleRoots_returnsAllRoots() {
    UUID cat1 = UUID.randomUUID();
    UUID cat2 = UUID.randomUUID();
    UUID cat3 = UUID.randomUUID();
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of(
            cat(cat1, "Electronics", "1"),
            cat(cat2, "Clothing",    "2"),
            cat(cat3, "Food",        "3")
    ));

    List<CategoryTreeDto> result = categoryService.getFullTree();

    assertEquals(3, result.size());
    assertEquals("Electronics", result.get(0).title);
    assertEquals("Clothing",    result.get(1).title);
    assertEquals("Food",        result.get(2).title);
  }

  @Test
  void getFullTree_rootWithChildren_wiresChildrenCorrectly() {
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of(
            cat(UUID.randomUUID(), "Electronics", "1"),
            cat(UUID.randomUUID(), "Phones",      "1.2"),
            cat(UUID.randomUUID(), "Laptops",     "1.3")
    ));

    List<CategoryTreeDto> result = categoryService.getFullTree();

    assertEquals(1, result.size());
    List<CategoryTreeDto> children = result.get(0).children;
    assertEquals(2, children.size());
    assertEquals("Phones",  children.get(0).title);
    assertEquals("Laptops", children.get(1).title);
  }

  @Test
  void getFullTree_deepNesting_wiresAllLevels() {
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of(
            cat(UUID.randomUUID(), "Electronics", "1"),
            cat(UUID.randomUUID(), "Phones",      "1.2"),
            cat(UUID.randomUUID(), "Smartphones", "1.2.3")
    ));

    List<CategoryTreeDto> result = categoryService.getFullTree();

    CategoryTreeDto electronics = result.get(0);
    CategoryTreeDto phones      = electronics.children.get(0);
    CategoryTreeDto smartphones = phones.children.get(0);

    assertEquals("Electronics", electronics.title);
    assertEquals("Phones",      phones.title);
    assertEquals("Smartphones", smartphones.title);
    assertTrue(smartphones.children.isEmpty());
  }

  @Test
  void getFullTree_mixedRootsAndChildren_structuresCorrectly() {
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of(
            cat(UUID.randomUUID(), "Electronics", "1"),
            cat(UUID.randomUUID(), "Phones",      "1.2"),
            cat(UUID.randomUUID(), "Clothing",    "3"),
            cat(UUID.randomUUID(), "Shirts",      "3.4")
    ));

    List<CategoryTreeDto> result = categoryService.getFullTree();

    assertEquals(2, result.size());

    assertEquals(1, result.get(0).children.size());
    assertEquals("Phones", result.get(0).children.get(0).title);

    assertEquals(1, result.get(1).children.size());
    assertEquals("Shirts", result.get(1).children.get(0).title);
  }

  @Test
  void getFullTree_orphanedNode_isSkipped() {
    // parent "1.2" is missing — simulates data inconsistency
    when(categoryRepository.fetchAllFlat()).thenReturn(List.of(
            cat(UUID.randomUUID(), "Electronics", "1"),
            cat(UUID.randomUUID(), "Smartphones", "1.2.3") // parent 1.2 doesn't exist
    ));

    List<CategoryTreeDto> result = categoryService.getFullTree();

    assertEquals(1, result.size());
    assertTrue(result.get(0).children.isEmpty());
  }

}
