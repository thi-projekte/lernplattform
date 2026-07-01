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

import de.thi.mynd.demoContent.event.LoadedCategoriesEvent;
import de.thi.mynd.topic.importer.ImportContext;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * {@link TopicLoader#initializeTopics} is normally fired once via the {@code LoadedCategoriesEvent}
 * emitted at app boot, when demo content is loaded into an empty database. By the time any
 * {@code @QuarkusTest} runs, that has already happened, so calling it again here directly exercises
 * the idempotency guard (repository already non-empty) rather than re-triggering it via the CDI
 * event bus.
 */
@QuarkusTest
class TopicLoaderTest {

  @Inject TopicLoader topicLoader;

  @Inject TopicRepository topicRepository;

  @Test
  void initializeTopics_alreadyInitialized_isANoOp() throws Exception {
    long countBefore = topicRepository.count();
    assertTrue(countBefore > 0, "demo content should already be loaded at this point");

    topicLoader.initializeTopics(new LoadedCategoriesEvent(new ImportContext(true)));

    assertEquals(countBefore, topicRepository.count());
  }
}
