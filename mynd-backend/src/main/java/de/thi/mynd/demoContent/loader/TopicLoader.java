/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.demoContent.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.demoContent.event.LoadedCategoriesEvent;
import de.thi.mynd.demoContent.event.LoadedTopicsEvent;
import de.thi.mynd.topic.dto.importer.ImportTopicDto;
import de.thi.mynd.topic.importer.ImportContext;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.service.ImportService;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@ApplicationScoped
@LookupIfProperty(name = "mynd.loadDemoContent", stringValue = "true")
public final class TopicLoader {

  @Inject Event<LoadedTopicsEvent> topicsEventEvent;

  @Inject TopicRepository topicRepository;

  @Inject ImportService importService;

  @Inject ObjectMapper mapper;

  @Transactional
  public void initializeTopics(@Observes LoadedCategoriesEvent event) throws IOException {
    if (topicRepository.count() > 0) {
      Log.info("Topics are already initialized");
      return;
    }

    List<ImportTopicDto> topics = loadJson();
    ImportContext ctx = importService.importTopics(topics, event.ctx());

    Log.info("Successfully initialized topics");
    topicsEventEvent.fire(new LoadedTopicsEvent(ctx));
  }

  private List<ImportTopicDto> loadJson() throws IOException {
    try (InputStream is =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("demo-content/topics.json")) {
      return mapper.readValue(is, new TypeReference<List<ImportTopicDto>>() {});
    }
  }
}
