package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.AudioFileElementDto;
import de.thi.mynd.topic.entity.AudioFileElement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class AudioFileElementDtoMapper
    extends AbstractMappingProcessor<AudioFileElement, AudioFileElementDto> {

  @Inject ObjectStorageService objectStorageService;

  @Override
  public AudioFileElementDto mapAndEnrich(AudioFileElement entity) {
    return AudioFileElementDto.builder()
        .id(entity.id)
        .title(entity.title)
            .icon(entity.icon)
        .createdAt(entity.createdAt)
        .updatedAt(entity.updatedAt)
        .originalFileName(entity.originalFileName)
        .presignedUrl(objectStorageService.getPresignedUrlForFile(entity.s3Key))
        .build();
  }

  @Override
  public Class<AudioFileElement> getEntityType() {
    return AudioFileElement.class;
  }

  @Override
  public Class<AudioFileElementDto> getDtoType() {
    return AudioFileElementDto.class;
  }
}
