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

import de.thi.mynd.topic.dto.content.SpotifyLinkElementDto;
import de.thi.mynd.topic.entity.SpotifyLinkElement;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SpotifyLinkElementDtoMapperTest {

  @Inject SpotifyLinkElementDtoMapper spotifyLinkElementDtoMapper;

  private SpotifyLinkElement spotifyLinkElement() {
    SpotifyLinkElement element = new SpotifyLinkElement();
    element.id = UUID.randomUUID();
    element.title = "Focus Playlist";
    element.icon = "music";
    element.rank = 3;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.uri = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    SpotifyLinkElement element = spotifyLinkElement();

    SpotifyLinkElementDto dto = spotifyLinkElementDtoMapper.mapAndEnrich(element);

    assertEquals(element.id, dto.id);
    assertEquals(element.title, dto.title);
    assertEquals(element.icon, dto.icon);
    assertEquals(element.rank, dto.rank);
    assertEquals(element.createdAt, dto.createdAt);
    assertEquals(element.updatedAt, dto.updatedAt);
    assertEquals(element.uri, dto.uri);
  }

  @Test
  void getEntityType_returnsSpotifyLinkElement() {
    assertEquals(SpotifyLinkElement.class, spotifyLinkElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsSpotifyLinkElementDto() {
    assertEquals(SpotifyLinkElementDto.class, spotifyLinkElementDtoMapper.getDtoType());
  }
}
