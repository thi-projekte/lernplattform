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
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.PdfElementDto;
import de.thi.mynd.topic.entity.PdfElement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class PdfElementDtoMapper extends AbstractMappingProcessor<PdfElement, PdfElementDto> {

  @Inject ObjectStorageService objectStorageService;

  @Override
  public PdfElementDto mapAndEnrich(PdfElement entity) {
    return PdfElementDto.builder()
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
  public Class<PdfElement> getEntityType() {
    return PdfElement.class;
  }

  @Override
  public Class<PdfElementDto> getDtoType() {
    return PdfElementDto.class;
  }
}
