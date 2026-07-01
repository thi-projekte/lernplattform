/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.dto.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GraphTopicDtoTest {

  @Test
  void equals_sameId_returnsTrue() {
    UUID id = UUID.randomUUID();
    GraphTopicDto a = GraphTopicDto.builder().id(id).build();
    GraphTopicDto b = GraphTopicDto.builder().id(id).build();

    assertEquals(a, b);
  }

  @Test
  void equals_differentId_returnsFalse() {
    GraphTopicDto a = GraphTopicDto.builder().id(UUID.randomUUID()).build();
    GraphTopicDto b = GraphTopicDto.builder().id(UUID.randomUUID()).build();

    assertNotEquals(a, b);
  }

  @Test
  void equals_nonGraphTopicDto_returnsFalse() {
    GraphTopicDto a = GraphTopicDto.builder().id(UUID.randomUUID()).build();

    assertNotEquals(a, "not a GraphTopicDto");
  }
}
