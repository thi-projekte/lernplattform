package de.thi.mynd.demoContent.event;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.importer.ImportContext;

import java.util.Map;

public record LoadedCategoriesEvent(ImportContext ctx) {}
