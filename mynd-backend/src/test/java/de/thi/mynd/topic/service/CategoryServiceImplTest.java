package de.thi.mynd.topic.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.CategoryDto;
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
    List<CategoryDto> results = categoryService.findByAssociatedEntities(requests);

    // Assert
    Assertions.assertEquals(2, results.size());
    verify(categoryRepository)
        .findByIdsTypeSafe(
            argThat(list -> list.contains(id1) && list.contains(id2) && list.size() == 2));
  }
}
