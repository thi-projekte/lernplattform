package de.thi.mynd.topic.processor.content;

import de.thi.mynd.common.service.ObjectStorageService;
import de.thi.mynd.topic.entity.AudioFileElement;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.requests.content.AudioFileElementRequest;
import de.thi.mynd.topic.requests.content.ContentElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;

@ApplicationScoped
public class AudioFileElementProcessor implements ContentElementProcessor<AudioFileElementRequest> {

    @Inject
    ObjectStorageService storageService;

    @Inject
    ContentElementRepository contentElementRepository;

    @Override
    public ContentElement creteContentElementFromRequest(AudioFileElementRequest request, File file) {

        try {
            AudioFileElement contentElement = new AudioFileElement();
            contentElement.title = request.title;
            contentElement.type = ContentType.AudioFile;
            contentElement.s3Key = storageService.uploadObject(contentElement, file);
            contentElement.originalFileName = request.originalFileName;

            contentElementRepository.persistAndFlush(contentElement);

            return contentElement;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean supports(ContentElementRequest request) {
        return request instanceof AudioFileElementRequest;
    }
}
