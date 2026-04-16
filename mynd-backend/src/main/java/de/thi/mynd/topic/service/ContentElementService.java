package de.thi.mynd.topic.service;

import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.content.ContentElementRequest;

import java.io.File;

public interface ContentElementService {

    ContentElementDto createContentElement(ContentElementRequest request, File uploadedFile);
}
