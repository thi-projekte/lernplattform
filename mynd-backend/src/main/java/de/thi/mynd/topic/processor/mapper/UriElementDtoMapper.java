/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.content.UriElementDto;
import de.thi.mynd.topic.entity.UriElement;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class UriElementDtoMapper extends AbstractMappingProcessor<UriElement, UriElementDto> {

  @Override
  public UriElementDto mapAndEnrich(UriElement entity) {
    return UriElementDto.builder()
        .id(entity.id)
        .title(entity.title)
        .icon(entity.icon)
        .rank(entity.rank)
        .createdAt(entity.createdAt)
        .updatedAt(entity.updatedAt)
        .uri(entity.uri)
        .build();
  }

  @Override
  public Class<UriElement> getEntityType() {
    return UriElement.class;
  }

  @Override
  public Class<UriElementDto> getDtoType() {
    return UriElementDto.class;
  }
}
