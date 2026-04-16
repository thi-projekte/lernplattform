package de.thi.mynd.topic.service;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.content.ContentElementRequest;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface ContentElementService {

    ContentElementDto createContentElement(ContentElementRequest request, File uploadedFile);

    List<ContentElementDto> getContentElementsForTopic(UUID topicId);

    void deleteContentElement(UUID elementId);
}
