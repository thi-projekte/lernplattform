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
import de.thi.mynd.topic.dto.content.RtfElementDto;
import de.thi.mynd.topic.entity.RtfElement;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class RtfElementDtoMapper extends AbstractMappingProcessor<RtfElement, RtfElementDto> {

  @Override
  public RtfElementDto mapAndEnrich(RtfElement entity) {
    return RtfElementDto.builder()
        .id(entity.id)
        .title(entity.title)
        .icon(entity.icon)
        .rank(entity.rank)
        .createdAt(entity.createdAt)
        .updatedAt(entity.updatedAt)
        .rtfText(entity.rtfText)
        .build();
  }

  @Override
  public Class<RtfElement> getEntityType() {
    return RtfElement.class;
  }

  @Override
  public Class<RtfElementDto> getDtoType() {
    return RtfElementDto.class;
  }
}
