/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.dto.content.AudioFileElementDto;
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.dto.content.ImageElementDto;
import de.thi.mynd.topic.dto.content.PdfElementDto;
import de.thi.mynd.topic.dto.content.RtfElementDto;
import de.thi.mynd.topic.dto.content.YouTubeLinkElementDto;
import de.thi.mynd.topic.entity.AudioFileElement;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.ImageElement;
import de.thi.mynd.topic.entity.PdfElement;
import de.thi.mynd.topic.entity.RtfElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.YouTubeLinkElement;
import de.thi.mynd.topic.processor.content.ContentElementProcessorManager;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.AssociatedContentElementRequest;
import de.thi.mynd.topic.request.content.PdfElementRequest;
import io.quarkus.security.ForbiddenException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContentElementServiceImplTest {

  @Inject ContentElementServiceImpl contentElementService;

  @InjectMock ContentElementProcessorManager processorManager;

  @InjectMock ContentElementRepository contentElementRepository;

  @InjectMock ObjectStorageService objectStorageService;

  @InjectMock MappingRegistry mappingRegistry;

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
  @TestSecurity(user = "creator")
  void testDeleteContentElement_WithS3Files() {
    // Arrange
    UUID id = UUID.randomUUID();
    PdfElement element = new PdfElement();
    element.id = id;
    element.s3Key = "path/to/file.pdf";
    element.creatorId = "creator";

    when(contentElementRepository.findById(id)).thenReturn(element);

    // Act
    contentElementService.deleteContentElement(id);

    // Assert
    verify(objectStorageService).tryDeleteObject("path/to/file.pdf");
    verify(contentElementRepository).delete(element);
  }

  @Test
  @TestSecurity(user = "creator1")
  void testDeleteContentElement_AsInvalidUser() {
    // Arrange
    UUID id = UUID.randomUUID();
    PdfElement element = new PdfElement();
    element.id = id;
    element.s3Key = "path/to/file.pdf";
    element.creatorId = "creator";

    when(contentElementRepository.findById(id)).thenReturn(element);

    Assertions.assertThrows(
        ForbiddenException.class, () -> contentElementService.deleteContentElement(id));
  }

  @Test
  void testDeleteContentElement_NotFound() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(contentElementRepository.findById(id)).thenReturn(null);

    // Act & Assert
    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> contentElementService.deleteContentElement(id));
  }

  @Test
  @TestSecurity(user = "creator")
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

    // Act
    contentElementService.updateTopicAssociation(topic, List.of(assocRequest));

    // Assert
    assertEquals(topic, element.topic);
    assertEquals(5, element.rank);
    verify(contentElementRepository).persist(element);
    verify(contentElementRepository).flush();
  }

  @Test
  @TestSecurity(user = "creator2")
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

    Assertions.assertThrows(
        ForbiddenException.class,
        () -> contentElementService.updateTopicAssociation(topic, List.of(assocRequest)));
  }

  @Test
  @DisplayName("returns the entity when it exists")
  void returnsEntityWhenFound() {
    UUID id = UUID.randomUUID();
    ContentElement expected = new PdfElement();
    expected.id = id;

    when(contentElementRepository.findByIdOptional(id)).thenReturn(Optional.of(expected));

    ContentElement result = contentElementService.getContentElementEntityById(id);

    assertNotNull(result);
    assertEquals(id, result.id);
    verify(contentElementRepository, times(1)).findByIdOptional(id);
  }

  @Test
  @DisplayName("throws EntityInstanceNotFoundException when entity is absent")
  void throwsWhenNotFound() {
    UUID id = UUID.randomUUID();

    when(contentElementRepository.findByIdOptional(id)).thenReturn(Optional.empty());

    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> contentElementService.getContentElementEntityById(id));

    verify(contentElementRepository, times(1)).findByIdOptional(id);
  }

  @Test
  @DisplayName("passes the exact id to the repository without modification")
  void forwardsIdUnchanged() {
    UUID id = UUID.randomUUID();

    when(contentElementRepository.findByIdOptional(any())).thenReturn(Optional.empty());

    assertThrows(
        EntityInstanceNotFoundException.class,
        () -> contentElementService.getContentElementEntityById(id));

    // Verify the exact same UUID reference was forwarded
    verify(contentElementRepository).findByIdOptional(id);
  }

  @Test
  @DisplayName("returns the count provided by the repository")
  void returnsRepositoryCount() {
    UUID topicId = UUID.randomUUID();

    when(contentElementRepository.countForTopic(topicId)).thenReturn(5L);

    long result = contentElementService.getCountOfElementsForTopicId(topicId);

    assertEquals(5L, result);
    verify(contentElementRepository, times(1)).countForTopic(topicId);
  }

  @Test
  @DisplayName("returns zero when topic has no content elements")
  void returnsZeroWhenNoElements() {
    UUID topicId = UUID.randomUUID();

    when(contentElementRepository.countForTopic(topicId)).thenReturn(0L);

    long result = contentElementService.getCountOfElementsForTopicId(topicId);

    assertEquals(0L, result);
  }

  @Test
  @DisplayName("passes the exact topic id to the repository without modification")
  void forwardsTopicIdUnchanged() {
    UUID topicId = UUID.randomUUID();

    when(contentElementRepository.countForTopic(any())).thenReturn(0L);

    contentElementService.getCountOfElementsForTopicId(topicId);

    verify(contentElementRepository).countForTopic(topicId);
  }

  @Test
  @DisplayName("propagates repository exceptions without wrapping")
  void propagatesRepositoryException() {
    UUID topicId = UUID.randomUUID();

    when(contentElementRepository.countForTopic(topicId))
        .thenThrow(new RuntimeException("DB unavailable"));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> contentElementService.getCountOfElementsForTopicId(topicId));
    assertEquals("DB unavailable", ex.getMessage());
  }

  @Test
  @DisplayName("getContentElementsForTopic delegates to repository and maps the results")
  void getContentElementsForTopicDelegatesAndMaps() {
    UUID topicId = UUID.randomUUID();
    PdfElement element = new PdfElement();
    element.id = UUID.randomUUID();
    element.type = ContentType.PDF;
    List<ContentElement> elements = List.of(element);
    List<ContentElementDto> dtos = List.of(PdfElementDto.builder().build());

    when(contentElementRepository.findForTopic(topicId)).thenReturn(elements);
    when(mappingRegistry.mapList(eq(elements), any(Function.class))).thenReturn(dtos);

    List<ContentElementDto> result = contentElementService.getContentElementsForTopic(topicId);

    assertEquals(dtos, result);
    verify(contentElementRepository).findForTopic(topicId);
    verify(mappingRegistry).mapList(eq(elements), any(Function.class));
  }

  @Test
  @DisplayName("createContentElement maps AudioFileElement to AudioFileElementDto")
  void createContentElementMapsAudioFileElement() {
    PdfElementRequest request = new PdfElementRequest();
    FileUpload file = mock(FileUpload.class);
    AudioFileElement element = new AudioFileElement();
    element.id = UUID.randomUUID();
    element.type = ContentType.AUDIO_FILE;
    AudioFileElementDto expectedDto = AudioFileElementDto.builder().build();

    when(processorManager.createContentElementFromRequest(request, file)).thenReturn(element);
    when(mappingRegistry.map(eq(element), eq(AudioFileElementDto.class))).thenReturn(expectedDto);

    ContentElementDto result = contentElementService.createContentElement(request, file);

    assertNotNull(result);
    verify(mappingRegistry).map(element, AudioFileElementDto.class);
  }

  @Test
  @DisplayName("createContentElement maps ImageElement to ImageElementDto")
  void createContentElementMapsImageElement() {
    PdfElementRequest request = new PdfElementRequest();
    FileUpload file = mock(FileUpload.class);
    ImageElement element = new ImageElement();
    element.id = UUID.randomUUID();
    element.type = ContentType.IMAGE;
    ImageElementDto expectedDto = ImageElementDto.builder().build();

    when(processorManager.createContentElementFromRequest(request, file)).thenReturn(element);
    when(mappingRegistry.map(eq(element), eq(ImageElementDto.class))).thenReturn(expectedDto);

    ContentElementDto result = contentElementService.createContentElement(request, file);

    assertNotNull(result);
    verify(mappingRegistry).map(element, ImageElementDto.class);
  }

  @Test
  @DisplayName("createContentElement maps RtfElement to RtfElementDto")
  void createContentElementMapsRtfElement() {
    PdfElementRequest request = new PdfElementRequest();
    FileUpload file = mock(FileUpload.class);
    RtfElement element = new RtfElement();
    element.id = UUID.randomUUID();
    element.type = ContentType.RTF;
    RtfElementDto expectedDto = RtfElementDto.builder().build();

    when(processorManager.createContentElementFromRequest(request, file)).thenReturn(element);
    when(mappingRegistry.map(eq(element), eq(RtfElementDto.class))).thenReturn(expectedDto);

    ContentElementDto result = contentElementService.createContentElement(request, file);

    assertNotNull(result);
    verify(mappingRegistry).map(element, RtfElementDto.class);
  }

  @Test
  @DisplayName("createContentElement maps YouTubeLinkElement to YouTubeLinkElementDto")
  void createContentElementMapsYouTubeLinkElement() {
    PdfElementRequest request = new PdfElementRequest();
    FileUpload file = mock(FileUpload.class);
    YouTubeLinkElement element = new YouTubeLinkElement();
    element.id = UUID.randomUUID();
    element.type = ContentType.YOUTUBE_LINK;
    YouTubeLinkElementDto expectedDto = YouTubeLinkElementDto.builder().build();

    when(processorManager.createContentElementFromRequest(request, file)).thenReturn(element);
    when(mappingRegistry.map(eq(element), eq(YouTubeLinkElementDto.class))).thenReturn(expectedDto);

    ContentElementDto result = contentElementService.createContentElement(request, file);

    assertNotNull(result);
    verify(mappingRegistry).map(element, YouTubeLinkElementDto.class);
  }

  private static final class UnknownContentElement extends ContentElement {
    // `type` is normally populated by Hibernate's discriminator column on read and is never set
    // for an in-memory-only instance; createContentElement logs contentElement.type.label before
    // routing, so it must be non-null here. The switch that picks the DTO class routes on the
    // Java type, not on this enum value, so the specific value is arbitrary. The field can only
    // be set from within a subclass (Hibernate's bytecode enhancement downgrades it to
    // protected), hence this constructor instead of setting it directly in the test.
    UnknownContentElement() {
      this.type = ContentType.PDF;
    }
  }

  @Test
  @DisplayName("createContentElement falls back to ContentElementDto for unknown subtypes")
  void createContentElementMapsUnknownSubtypeToDefaultDto() {
    PdfElementRequest request = new PdfElementRequest();
    FileUpload file = mock(FileUpload.class);
    UnknownContentElement element = new UnknownContentElement();
    element.id = UUID.randomUUID();

    when(processorManager.createContentElementFromRequest(request, file)).thenReturn(element);
    when(mappingRegistry.map(eq(element), eq(ContentElementDto.class))).thenReturn(null);

    contentElementService.createContentElement(request, file);

    verify(mappingRegistry).map(element, ContentElementDto.class);
  }
}
