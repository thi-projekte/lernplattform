/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.processor;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MappingRegistryTest {

  @Inject MappingRegistry mappingRegistry;

  @Test
  void testMap_Success() {
    SourceEntity entity = new SourceEntity();
    // This will now use the "MockProcessor" defined below
    TargetDto result = mappingRegistry.map(entity, TargetDto.class);

    assertNotNull(result);
    assertEquals("Mocked", result.name);
  }

  @Test
  void map_nullEntity_returnsNull() {
    TargetDto result = mappingRegistry.map((SourceEntity) null, TargetDto.class);

    assertNull(result);
  }

  @Test
  void map_noProcessorForEntityAndDtoCombination_throwsIllegalArgumentException() {
    SourceEntity entity = new SourceEntity();

    assertThrows(
        IllegalArgumentException.class, () -> mappingRegistry.map(entity, UnmappedDto.class));
  }

  @Test
  void map_withAdditionalData_passesDataThroughToProcessor() {
    EnrichableDto result =
        mappingRegistry.map(new EnrichableEntity(), EnrichableDto.class, "explicit-data");

    assertEquals("explicit-data", result.extra);
  }

  @Test
  void mapList_nullList_returnsEmptyList() {
    assertTrue(mappingRegistry.mapList(null, TargetDto.class).isEmpty());
  }

  @Test
  void mapList_emptyList_returnsEmptyList() {
    assertTrue(mappingRegistry.mapList(List.of(), TargetDto.class).isEmpty());
  }

  @Test
  void mapList_nonEmptyList_mapsEachEntity() {
    List<TargetDto> result =
        mappingRegistry.mapList(List.of(new SourceEntity(), new SourceEntity()), TargetDto.class);

    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(dto -> dto.name.equals("Mocked")));
  }

  @Test
  void mapListVarargsOverload_nullList_returnsEmptyList() {
    assertTrue(mappingRegistry.mapList(null, EnrichableDto.class, "explicit-data").isEmpty());
  }

  @Test
  void mapListVarargsOverload_passesAdditionalDataToEachEntity() {
    List<EnrichableDto> result =
        mappingRegistry.mapList(
            List.of(new EnrichableEntity()), EnrichableDto.class, "explicit-data");

    assertEquals(1, result.size());
    assertEquals("explicit-data", result.get(0).extra);
  }

  @Test
  void mapListWithTypeResolver_nullList_returnsEmptyList() {
    assertTrue(mappingRegistry.mapList(null, e -> TargetDto.class).isEmpty());
  }

  @Test
  void mapListWithTypeResolver_resolvesDtoTypePerEntity() {
    List<? extends Object> result =
        mappingRegistry.mapList(List.of(new SourceEntity()), e -> TargetDto.class);

    assertEquals(1, result.size());
    assertInstanceOf(TargetDto.class, result.get(0));
  }

  @Test
  void mapListWithAdditionalData_emptyList_returnsEmptyList() {
    assertTrue(mappingRegistry.mapListWithAdditionalData(List.of(), TargetDto.class).isEmpty());
  }

  @Test
  void mapListWithAdditionalData_processorWithoutOverride_fallsBackToPlainMapping() {
    // MockProcessor does not override obtainAdditionalData, so the base implementation throws
    // ObtainNotImplementedException, which tryObtainAdditionalData must swallow before falling
    // back to a plain per-entity map.
    List<TargetDto> result =
        mappingRegistry.mapListWithAdditionalData(List.of(new SourceEntity()), TargetDto.class);

    assertEquals(1, result.size());
    assertEquals("Mocked", result.get(0).name);
  }

  @Test
  void mapListWithAdditionalData_processorOverridingObtainAdditionalData_usesEnrichedData() {
    List<EnrichableDto> result =
        mappingRegistry.mapListWithAdditionalData(
            List.of(new EnrichableEntity()), EnrichableDto.class);

    assertEquals(1, result.size());
    assertEquals("computed", result.get(0).extra);
  }

  // Define a real CDI bean inside the test class
  @ApplicationScoped
  static class MockProcessor extends AbstractMappingProcessor<SourceEntity, TargetDto> {
    @Override
    public Class<SourceEntity> getEntityType() {
      return SourceEntity.class;
    }

    @Override
    public Class<TargetDto> getDtoType() {
      return TargetDto.class;
    }

    @Override
    public TargetDto mapAndEnrich(SourceEntity entity) {
      return new TargetDto("Mocked");
    }
  }

  static class SourceEntity {}

  static class TargetDto {
    String name;

    TargetDto(String name) {
      this.name = name;
    }
  }

  static class UnmappedDto {}

  @ApplicationScoped
  static class EnrichableProcessor
      extends AbstractMappingProcessor<EnrichableEntity, EnrichableDto> {
    @Override
    public Class<EnrichableEntity> getEntityType() {
      return EnrichableEntity.class;
    }

    @Override
    public Class<EnrichableDto> getDtoType() {
      return EnrichableDto.class;
    }

    @Override
    public EnrichableDto mapAndEnrich(EnrichableEntity entity) {
      return new EnrichableDto(null);
    }

    @Override
    public EnrichableDto mapAndEnrich(EnrichableEntity entity, Object... additionalData) {
      return new EnrichableDto((String) additionalData[0]);
    }

    @Override
    public Object[] obtainAdditionalData(List<EnrichableEntity> entities) {
      return new Object[] {"computed"};
    }
  }

  static class EnrichableEntity {}

  static class EnrichableDto {
    String extra;

    EnrichableDto(String extra) {
      this.extra = extra;
    }
  }
}
