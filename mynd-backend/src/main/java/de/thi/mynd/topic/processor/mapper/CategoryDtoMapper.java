/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.entity.Category;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class CategoryDtoMapper extends AbstractMappingProcessor<Category, CategoryDto> {
  @Override
  public CategoryDto mapAndEnrich(Category entity) {
    return CategoryDto.builder().id(entity.id).title(entity.title).color(entity.color).build();
  }

  @Override
  public Class<Category> getEntityType() {
    return Category.class;
  }

  @Override
  public Class<CategoryDto> getDtoType() {
    return CategoryDto.class;
  }
}
