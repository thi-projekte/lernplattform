package de.thi.mynd.topic.service;

import de.thi.mynd.topic.dto.loader.FullImportDto;
import de.thi.mynd.topic.dto.loader.ImportCategoryDto;
import de.thi.mynd.topic.dto.loader.ImportTopicDto;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ImportService {

    void setBackendMode(Boolean backendMode);

    void importTopicJson(FullImportDto importDto);

    void importTopicJsonFromRequest(InputStream inputStream);

    void importCategories(List<ImportCategoryDto> categoryDtos);

    void importTopics(List<ImportTopicDto> topicDtos);

    void importTopicAssociations(Map<String, List<String>> topicAssociations);
}
