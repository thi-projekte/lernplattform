package de.thi.mynd.topic.service;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface ContentElementService {

  ContentElementDto createContentElement(ContentElementRequest request, FileUpload uploadedFile);

  List<ContentElementDto> getContentElementsForTopic(UUID topicId);

  void deleteContentElement(UUID elementId);
}
