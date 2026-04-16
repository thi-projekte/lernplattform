package de.thi.mynd.topic.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.processor.content.ContentElementProcessorManager;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;

@ApplicationScoped
public class ContentElementServiceImpl implements ContentElementService {

    @Inject
    ContentElementProcessorManager contentElementProcessorManager;

    @Inject
    MappingRegistry mappingRegistry;


    @Override
    public ContentElementDto createContentElement(ContentElementRequest request, File file) {
        ContentElement contentElement = contentElementProcessorManager.createContentElementFromRequest(request, file);

        // TODO: Add all other processors too. Then also add processing for the DTOs

        return mappingRegistry.map(contentElement, ContentElementDto.class);
    }
}
