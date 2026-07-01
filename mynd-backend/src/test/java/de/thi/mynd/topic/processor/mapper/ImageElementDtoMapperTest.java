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
import de.thi.mynd.topic.dto.content.ImageElementDto;
import de.thi.mynd.topic.entity.ImageElement;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ImageElementDtoMapperTest {

  @Inject ImageElementDtoMapper imageElementDtoMapper;

  @InjectMock ObjectStorageService objectStorageService;

  private ImageElement imageElement() {
    ImageElement element = new ImageElement();
    element.id = UUID.randomUUID();
    element.title = "Diagram";
    element.icon = "image";
    element.rank = 6;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.s3Key = "images/diagram.png";
    element.originalFileName = "diagram.png";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllFieldsAndResolvesPresignedUrl() throws MalformedURLException {
    ImageElement element = imageElement();
    URL presignedUrl = new URL("https://storage.example.com/images/diagram.png?sig=abc");
    when(objectStorageService.getPresignedUrlForFile(element.s3Key)).thenReturn(presignedUrl);

    ImageElementDto dto = imageElementDtoMapper.mapAndEnrich(element);

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
  void getEntityType_returnsImageElement() {
    assertEquals(ImageElement.class, imageElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsImageElementDto() {
    assertEquals(ImageElementDto.class, imageElementDtoMapper.getDtoType());
  }
}
