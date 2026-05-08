package de.thi.mynd.topic.processor.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.topic.dto.content.SpotifyLinkElementDto;
import de.thi.mynd.topic.entity.SpotifyLinkElement;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class SpotifyLinkElementDtoMapper
    extends AbstractMappingProcessor<SpotifyLinkElement, SpotifyLinkElementDto> {

  @Override
  public SpotifyLinkElementDto mapAndEnrich(SpotifyLinkElement entity) {
    return SpotifyLinkElementDto.builder()
        .id(entity.id)
        .title(entity.title)
        .icon(entity.icon)
        .createdAt(entity.createdAt)
        .updatedAt(entity.updatedAt)
        .uri(entity.uri)
        .build();
  }

  @Override
  public Class<SpotifyLinkElement> getEntityType() {
    return SpotifyLinkElement.class;
  }

  @Override
  public Class<SpotifyLinkElementDto> getDtoType() {
    return SpotifyLinkElementDto.class;
  }
}
