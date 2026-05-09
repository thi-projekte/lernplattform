package de.thi.mynd.demoContent.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.demoContent.event.LoadedCategoriesEvent;
import de.thi.mynd.demoContent.models.CategoryModel;
import de.thi.mynd.demoContent.models.TopicModel;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@LookupIfProperty(name = "mynd.loadDemoContent", stringValue = "true")
public final class TopicLoader {

    @Inject
    TopicRepository topicRepository;

    @Inject
    ContentElementRepository contentElementRepository;

    @Inject
    ObjectMapper mapper;

    @Transactional
    public void initializeTopics(@Observes LoadedCategoriesEvent event) throws IOException {
        if (topicRepository.count() > 0) {
            Log.info("Topics are already initialized");
            return;
        }

        List<TopicModel> topics = loadJson();

        for (TopicModel model : topics) {
            Topic topic = new Topic();
            topic.title = model.getTitle();
            topic.teaser = model.getTeaser();
            topic.creatorId = model.getCreatorId();
            topic.estimatedLearningDuration = model.getDuration();
            topic.categories = model.getCategories().stream().map(c -> event.mapping().get(c)).toList();
            topicRepository.persist(topic);

            List<ContentElement> contentElements = model.getContentElements().stream().map(ce -> mapToContentElement(ce)).toList();
            for (ContentElement ce : contentElements) {
                ce.topic = topic;
                contentElementRepository.persist(ce);
            }
        }

        topicRepository.flush();

        Log.info("Successfully initialized topic");
    }

    private List<TopicModel> loadJson() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("demo-content/topics.json")) {
            return mapper.readValue(is, new TypeReference<List<TopicModel>>() {});
        }
    }

    private ContentElement mapToContentElement(Map<String, Object> generic) {
        return (ContentElement) mapper.convertValue(generic, getContentElementClass((String) generic.get("type")));
    }

    private Class getContentElementClass(String type) {
        return switch (type) {
            case "RTF" -> RtfElement.class;
            case "SPOTIFY_LINK" -> SpotifyLinkElement.class;
            case "URI" -> UriElement.class;
            case "YOUTUBE_LINK" -> YouTubeLinkElement.class;
            default -> RtfElement.class;
        };
    }
}
