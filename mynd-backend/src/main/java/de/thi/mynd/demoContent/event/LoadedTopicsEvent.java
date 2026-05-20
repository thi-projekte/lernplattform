package de.thi.mynd.demoContent.event;

import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.importer.ImportContext;

import java.util.Map;

public record LoadedTopicsEvent(ImportContext ctx) {}
