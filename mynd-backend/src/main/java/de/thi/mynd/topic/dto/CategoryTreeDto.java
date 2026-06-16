/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.thi.mynd.topic.entity.Category;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class CategoryTreeDto extends CategoryDto {
  public List<CategoryTreeDto> children;

  @JsonIgnore public String path;

  public static CategoryTreeDto from(Category category) {
    return CategoryTreeDto.builder()
        .id(category.id)
        .title(category.title)
        .color(category.color)
        .path(category.path)
        .children(new ArrayList<>())
        .build();
  }
}
