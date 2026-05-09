package de.thi.mynd.demoContent.event;

import de.thi.mynd.topic.entity.Category;

import java.util.Map;

public final record LoadedCategoriesEvent(Map<String, Category> mapping) {}
