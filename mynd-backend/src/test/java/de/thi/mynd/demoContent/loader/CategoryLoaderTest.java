/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.demoContent.loader;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.topic.repository.CategoryRepository;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * {@link CategoryLoader#initializeCategories} is normally fired once via {@code @Observes
 * StartupEvent} at app boot, when demo content is loaded into an empty database. By the time any
 * {@code @QuarkusTest} runs, that has already happened, so calling it again here directly exercises
 * the idempotency guard (repository already non-empty) rather than re-triggering it via the CDI
 * event bus.
 */
@QuarkusTest
class CategoryLoaderTest {

  @Inject CategoryLoader categoryLoader;

  @Inject CategoryRepository categoryRepository;

  @Test
  void initializeCategories_alreadyInitialized_isANoOp() throws Exception {
    long countBefore = categoryRepository.count();
    assertTrue(countBefore > 0, "demo content should already be loaded at this point");

    categoryLoader.initializeCategories(new StartupEvent());

    assertEquals(countBefore, categoryRepository.count());
  }
}
