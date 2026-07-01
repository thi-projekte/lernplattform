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
import static org.mockito.Mockito.*;

import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.AudioFileElementDto;
import de.thi.mynd.topic.entity.AudioFileElement;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AudioFileElementDtoMapperTest {

  @Inject AudioFileElementDtoMapper audioFileElementDtoMapper;

  @InjectMock ObjectStorageService objectStorageService;

  private AudioFileElement audioFileElement() {
    AudioFileElement element = new AudioFileElement();
    element.id = UUID.randomUUID();
    element.title = "Meditation";
    element.icon = "audio";
    element.rank = 5;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.s3Key = "audio/meditation.mp3";
    element.originalFileName = "meditation.mp3";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllFieldsAndResolvesPresignedUrl() throws MalformedURLException {
    AudioFileElement element = audioFileElement();
    URL presignedUrl = new URL("https://storage.example.com/audio/meditation.mp3?sig=abc");
    when(objectStorageService.getPresignedUrlForFile(element.s3Key)).thenReturn(presignedUrl);

    AudioFileElementDto dto = audioFileElementDtoMapper.mapAndEnrich(element);

    assertEquals(element.id, dto.id);
    assertEquals(element.title, dto.title);
    assertEquals(element.icon, dto.icon);
    assertEquals(element.rank, dto.rank);
    assertEquals(element.createdAt, dto.createdAt);
    assertEquals(element.updatedAt, dto.updatedAt);
    assertEquals(element.originalFileName, dto.originalFileName);
    assertEquals(presignedUrl, dto.presignedUrl);
    verify(objectStorageService).getPresignedUrlForFile(element.s3Key);
  }

  @Test
  void getEntityType_returnsAudioFileElement() {
    assertEquals(AudioFileElement.class, audioFileElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsAudioFileElementDto() {
    assertEquals(AudioFileElementDto.class, audioFileElementDtoMapper.getDtoType());
  }
}
