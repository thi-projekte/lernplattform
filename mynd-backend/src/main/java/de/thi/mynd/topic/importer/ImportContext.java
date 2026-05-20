package de.thi.mynd.topic.importer;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public final class ImportContext {

    @Getter
    private final boolean backendMode;
    @Getter
    private final Map<String, Category> categoryMapping;
    @Getter
    private final Map<String, Topic> topicMapping;

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
