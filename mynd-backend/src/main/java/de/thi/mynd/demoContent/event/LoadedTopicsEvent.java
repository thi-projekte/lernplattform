package de.thi.mynd.demoContent.event;

import de.thi.mynd.topic.importer.ImportContext;

public record LoadedTopicsEvent(ImportContext ctx) {}
