/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
}
