package de.thi.mynd.common.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MappingRegistryTest {

  @Inject MappingRegistry mappingRegistry;

  @InjectMock Instance<AbstractMappingProcessor<?, ?>> processors;

  // Dummy classes for testing
  static class SourceEntity {}

  static class TargetDto {
    String name;

    TargetDto(String name) {
      this.name = name;
    }
  }

  @Test
  void testMap_Success() {
    // Arrange
    SourceEntity entity = new SourceEntity();
    TargetDto dto = new TargetDto("Mapped");

    AbstractMappingProcessor<SourceEntity, TargetDto> mockProcessor =
        mock(AbstractMappingProcessor.class);
    when(mockProcessor.getEntityType()).thenReturn(SourceEntity.class);
    when(mockProcessor.getDtoType()).thenReturn(TargetDto.class);
    when(mockProcessor.mapAndEnrich(entity)).thenReturn(dto);

    // Mock the Instance stream to return our processor
    when(processors.stream()).thenAnswer(i -> Stream.of(mockProcessor));

    // Act
    TargetDto result = mappingRegistry.map(entity, TargetDto.class);

    // Assert
    assertNotNull(result);
    assertEquals("Mapped", result.name);
  }

  @Test
  void testMap_EntityIsNull() {
    // Act
    TargetDto result = mappingRegistry.map(null, TargetDto.class);

    // Assert
    assertNull(result);
  }

  @Test
  void testMap_NoProcessorFoundThrowsException() {
    // Arrange
    SourceEntity entity = new SourceEntity();
    when(processors.stream()).thenReturn(Stream.empty());

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mappingRegistry.map(entity, TargetDto.class);
        });
  }

  @Test
  void testMapList_Success() {
    // Arrange
    SourceEntity entity1 = new SourceEntity();
    SourceEntity entity2 = new SourceEntity();
    List<SourceEntity> entities = List.of(entity1, entity2);

    AbstractMappingProcessor<SourceEntity, TargetDto> mockProcessor =
        mock(AbstractMappingProcessor.class);
    when(mockProcessor.getEntityType()).thenReturn(SourceEntity.class);
    when(mockProcessor.getDtoType()).thenReturn(TargetDto.class);
    when(mockProcessor.mapAndEnrich(entity1)).thenReturn(new TargetDto("One"));
    when(mockProcessor.mapAndEnrich(entity2)).thenReturn(new TargetDto("Two"));

    when(processors.stream()).thenAnswer(i -> Stream.of(mockProcessor));

    // Act
    List<TargetDto> results = mappingRegistry.mapList(entities, TargetDto.class);

    // Assert
    assertEquals(2, results.size());
    assertEquals("One", results.get(0).name);
    assertEquals("Two", results.get(1).name);
  }

  @Test
  void testMapList_WithResolver() {
    // Arrange
    SourceEntity entity = new SourceEntity();

    AbstractMappingProcessor<SourceEntity, TargetDto> mockProcessor =
        mock(AbstractMappingProcessor.class);
    when(mockProcessor.getEntityType()).thenReturn(SourceEntity.class);
    when(mockProcessor.getDtoType()).thenReturn(TargetDto.class);
    when(mockProcessor.mapAndEnrich(entity)).thenReturn(new TargetDto("Resolved"));

    when(processors.stream()).thenAnswer(i -> Stream.of(mockProcessor));

    // Act
    List<? extends TargetDto> results =
        mappingRegistry.mapList(List.of(entity), e -> TargetDto.class);

    // Assert
    assertEquals(1, results.size());
    assertEquals("Resolved", results.get(0).name);
  }
}
