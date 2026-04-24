package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.VideoFileElementDto;
import de.thi.mynd.topic.entity.VideoFileElement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class VideoFileElementDtoMapper
    extends AbstractMappingProcessor<VideoFileElement, VideoFileElementDto> {

  @Inject ObjectStorageService objectStorageService;

  @Override
  public VideoFileElementDto mapAndEnrich(VideoFileElement entity) {
    return VideoFileElementDto.builder()
        .id(entity.id)
        .title(entity.title)
        .createdAt(entity.createdAt)
        .updatedAt(entity.updatedAt)
        .originalFileName(entity.originalFileName)
        .presignedUrl(objectStorageService.getPresignedUrlForFile(entity.s3Key))
        .build();
  }

  @Override
  public Class<VideoFileElement> getEntityType() {
    return VideoFileElement.class;
  }

  @Override
  public Class<VideoFileElementDto> getDtoType() {
    return VideoFileElementDto.class;
  }
}
