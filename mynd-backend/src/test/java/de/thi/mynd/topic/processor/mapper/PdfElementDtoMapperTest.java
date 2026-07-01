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
import de.thi.mynd.topic.dto.content.PdfElementDto;
import de.thi.mynd.topic.entity.PdfElement;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PdfElementDtoMapperTest {

  @Inject PdfElementDtoMapper pdfElementDtoMapper;

  @InjectMock ObjectStorageService objectStorageService;

  private PdfElement pdfElement() {
    PdfElement element = new PdfElement();
    element.id = UUID.randomUUID();
    element.title = "Lecture Slides";
    element.icon = "pdf";
    element.rank = 7;
    element.createdAt = LocalDateTime.now().minusDays(1);
    element.updatedAt = LocalDateTime.now();
    element.s3Key = "pdfs/lecture-slides.pdf";
    element.originalFileName = "lecture-slides.pdf";
    return element;
  }

  @Test
  void mapAndEnrich_copiesAllFieldsAndResolvesPresignedUrl() throws MalformedURLException {
    PdfElement element = pdfElement();
    URL presignedUrl = new URL("https://storage.example.com/pdfs/lecture-slides.pdf?sig=abc");
    when(objectStorageService.getPresignedUrlForFile(element.s3Key)).thenReturn(presignedUrl);

    PdfElementDto dto = pdfElementDtoMapper.mapAndEnrich(element);

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
  void getEntityType_returnsPdfElement() {
    assertEquals(PdfElement.class, pdfElementDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsPdfElementDto() {
    assertEquals(PdfElementDto.class, pdfElementDtoMapper.getDtoType());
  }
}
