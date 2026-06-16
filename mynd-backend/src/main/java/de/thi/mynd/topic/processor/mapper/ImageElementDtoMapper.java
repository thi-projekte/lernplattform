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
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.ImageElementDto;
import de.thi.mynd.topic.entity.ImageElement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class ImageElementDtoMapper
    extends AbstractMappingProcessor<ImageElement, ImageElementDto> {

  @Inject ObjectStorageService objectStorageService;

  @Override
  public ImageElementDto mapAndEnrich(ImageElement entity) {
    return ImageElementDto.builder()
        .id(entity.id)
        .title(entity.title)
        .icon(entity.icon)
        .rank(entity.rank)
        .createdAt(entity.createdAt)
        .updatedAt(entity.updatedAt)
        .originalFileName(entity.originalFileName)
        .presignedUrl(objectStorageService.getPresignedUrlForFile(entity.s3Key))
        .build();
  }

  @Override
  public Class<ImageElement> getEntityType() {
    return ImageElement.class;
  }

  @Override
  public Class<ImageElementDto> getDtoType() {
    return ImageElementDto.class;
  }
}
