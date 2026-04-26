package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.dto.content.PdfElementDto;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.PdfElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.processor.content.ContentElementProcessorManager;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.AssociatedContentElementRequest;
import de.thi.mynd.topic.requests.content.PdfElementRequest;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContentElementServiceImplTest {

  @Inject ContentElementServiceImpl contentElementService;

  @InjectMock ContentElementProcessorManager processorManager;

  @InjectMock ContentElementRepository contentElementRepository;

  @InjectMock ObjectStorageService objectStorageService;

  @InjectMock MappingRegistry mappingRegistry;

  @InjectMock SecurityIdentity securityIdentity;

  @Test
  void testCreateContentElement() {
    // Arrange
    PdfElementRequest request = new PdfElementRequest();
    FileUpload file = mock(FileUpload.class);
    PdfElement element = new PdfElement();
    element.id = UUID.randomUUID();
    element.type = ContentType.PDF;
    PdfElementDto expectedDto = PdfElementDto.builder().build();

    when(processorManager.createContentElementFromRequest(request, file)).thenReturn(element);
    when(mappingRegistry.map(eq(element), eq(PdfElementDto.class))).thenReturn(expectedDto);

    // Act
    ContentElementDto result = contentElementService.createContentElement(request, file);

    // Assert
    assertNotNull(result);
    verify(processorManager).createContentElementFromRequest(request, file);
    verify(mappingRegistry).map(element, PdfElementDto.class);
  }

  @Test
  void testDeleteContentElement_WithS3Files() {
    // Arrange
    UUID id = UUID.randomUUID();
    PdfElement element = new PdfElement();
    element.id = id;
    element.s3Key = "path/to/file.pdf";
    element.creatorId = "creator";

    when(contentElementRepository.findById(id)).thenReturn(element);
    when(securityIdentity.getPrincipal().getName()).thenReturn("creator");

    // Act
    contentElementService.deleteContentElement(id);

    // Assert
    verify(objectStorageService).tryDeleteObject("path/to/file.pdf");
    verify(contentElementRepository).delete(element);
  }

  @Test
  void testDeleteContentElement_AsInvalidUser() {
    // Arrange
    UUID id = UUID.randomUUID();
    PdfElement element = new PdfElement();
    element.id = id;
    element.s3Key = "path/to/file.pdf";
    element.creatorId = "creator";

    when(contentElementRepository.findById(id)).thenReturn(element);
    when(securityIdentity.getPrincipal().getName()).thenReturn("creator1");

    Assertions.assertThrows(
        ForbiddenException.class, () -> contentElementService.deleteContentElement(id));
  }

  @Test
  void testDeleteContentElement_NotFound() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(contentElementRepository.findById(id)).thenReturn(null);

    // Act & Assert
    assertThrows(NotFoundException.class, () -> contentElementService.deleteContentElement(id));
  }

  @Test
  void testUpdateTopicAssociation() {
    // Arrange
    Topic topic = new Topic();
    UUID elementId = UUID.randomUUID();
    AssociatedContentElementRequest assocRequest = new AssociatedContentElementRequest();
    assocRequest.id = elementId;
    assocRequest.rank = 5;

    PdfElement element = new PdfElement();
    element.creatorId = "creator";
    element.id = elementId;

    when(contentElementRepository.findByIdsTypeSafe(any())).thenReturn(List.of(element));
    when(securityIdentity.getPrincipal().getName()).thenReturn("creator");

    // Act
    contentElementService.updateTopicAssociation(topic, List.of(assocRequest));

    // Assert
    assertEquals(topic, element.topic);
    assertEquals(5, element.rank);
    verify(contentElementRepository).persist(element);
    verify(contentElementRepository).flush();
  }

  @Test
  void testUpdateTopicAssociationWithInvalidUser() {
    // Arrange
    Topic topic = new Topic();
    UUID elementId = UUID.randomUUID();
    AssociatedContentElementRequest assocRequest = new AssociatedContentElementRequest();
    assocRequest.id = elementId;
    assocRequest.rank = 5;

    PdfElement element = new PdfElement();
    element.creatorId = "creator";
    element.id = elementId;

    when(contentElementRepository.findByIdsTypeSafe(any())).thenReturn(List.of(element));
    when(securityIdentity.getPrincipal().getName()).thenReturn("creator2");

    Assertions.assertThrows(
        ForbiddenException.class,
        () -> contentElementService.updateTopicAssociation(topic, List.of(assocRequest)));
  }
}
