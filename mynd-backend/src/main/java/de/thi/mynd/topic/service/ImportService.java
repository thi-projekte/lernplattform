package de.thi.mynd.topic.service;

import de.thi.mynd.topic.dto.importer.FullImportDto;
import de.thi.mynd.topic.dto.importer.ImportCategoryDto;
import de.thi.mynd.topic.dto.importer.ImportTopicDto;
import de.thi.mynd.topic.importer.ImportContext;

import java.util.List;
import java.util.Map;

public interface ImportService {


    void importFull(FullImportDto importDto, boolean backendMode);

    ImportContext importCategories(List<ImportCategoryDto> categoryDtos, ImportContext ctx);

    ImportContext importTopics(List<ImportTopicDto> topicDtos, ImportContext context);

    void importAssociations(Map<String, List<String>> topicAssociations, ImportContext context);
}
