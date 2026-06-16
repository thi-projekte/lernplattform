/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.importer;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public final class ImportContext {

  @Getter private final boolean backendMode;
  @Getter private final Map<String, Category> categoryMapping;
  @Getter private final Map<String, Topic> topicMapping;

  public ImportContext(boolean backendMode) {
    this.backendMode = backendMode;
    this.categoryMapping = new HashMap<>();
    this.topicMapping = new HashMap<>();
  }

  public ImportContext withCategoryMapping(Map<String, Category> categoryMapping) {
    ImportContext next = new ImportContext(this.backendMode);
    next.categoryMapping.putAll(categoryMapping);
    return next;
  }

  public ImportContext withTopicMapping(Map<String, Topic> topicMapping) {
    ImportContext next = new ImportContext(this.backendMode);
    next.categoryMapping.putAll(this.categoryMapping);
    next.topicMapping.putAll(topicMapping);
    return next;
  }
}
