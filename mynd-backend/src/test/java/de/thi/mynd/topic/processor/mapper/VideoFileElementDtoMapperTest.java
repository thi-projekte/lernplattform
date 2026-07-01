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
import de.thi.mynd.topic.dto.content.VideoFileElementDto;
import de.thi.mynd.topic.entity.VideoFileElement;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class VideoFileElementDtoMapperTest {

  @Inject VideoFileElementDtoMapper videoFileElementDtoMapper;

  @InjectMock ObjectStorageService objectStorageService;

  private VideoFileElement videoFileElement() {
    VideoFileElement element = new VideoFileElement();
    element.id = UUID.randomUUID();
    element.title = "Lecture Recording";
    element.icon = "video";
    element.rank = 8;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.s3Key = "videos/lecture-recording.mp4";
    element.originalFileName = "lecture-recording.mp4";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllFieldsAndResolvesPresignedUrl() throws MalformedURLException {
    VideoFileElement element = videoFileElement();
    URL presignedUrl = new URL("https://storage.example.com/videos/lecture-recording.mp4?sig=abc");
    when(objectStorageService.getPresignedUrlForFile(element.s3Key)).thenReturn(presignedUrl);

    VideoFileElementDto dto = videoFileElementDtoMapper.mapAndEnrich(element);

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
  void getEntityType_returnsVideoFileElement() {
    assertEquals(VideoFileElement.class, videoFileElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsVideoFileElementDto() {
    assertEquals(VideoFileElementDto.class, videoFileElementDtoMapper.getDtoType());
  }
}
