package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.entity.BaseEntity;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.io.File;

@ApplicationScoped
public final class ContentElementProcessorManager {

    @Inject
    Instance<ContentElementProcessor<? extends ContentElementRequest>> processors;

    @SuppressWarnings("unchecked")
    public ContentElement createContentElementFromRequest(ContentElementRequest request, File file) {
        ContentElementProcessor<ContentElementRequest> processor = (ContentElementProcessor<ContentElementRequest>) processors.stream()
                .filter(p -> p.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No processor for type: " + request.getClass()));

        return processor.creteContentElementFromRequest(request, file);
    }
}
