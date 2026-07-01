/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.processor.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.entity.Category;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CategoryDtoMapperTest {

  @Inject CategoryDtoMapper categoryDtoMapper;

  private Category category() {
    Category category = new Category();
    category.id = UUID.randomUUID();
    category.title = "Mathematics";
    category.color = "#ff00ff";
    return category;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    Category category = category();

    CategoryDto dto = categoryDtoMapper.mapAndEnrich(category);

    assertEquals(category.id, dto.id);
    assertEquals(category.title, dto.title);
    assertEquals(category.color, dto.color);
  }

  @Test
  void getEntityType_returnsCategory() {
    assertEquals(Category.class, categoryDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsCategoryDto() {
    assertEquals(CategoryDto.class, categoryDtoMapper.getDtoType());
  }
}
