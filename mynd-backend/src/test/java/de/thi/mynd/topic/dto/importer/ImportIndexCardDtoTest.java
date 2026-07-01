/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.dto.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ImportIndexCardDtoTest {

  @Test
  void getters_returnAssignedFields() {
    ImportIndexCardDto dto = new ImportIndexCardDto();
    dto.question = "What is the capital of France?";
    dto.answer = "Paris";

    assertEquals("What is the capital of France?", dto.getQuestion());
    assertEquals("Paris", dto.getAnswer());
  }
}
