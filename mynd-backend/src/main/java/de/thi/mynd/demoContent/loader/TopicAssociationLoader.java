package de.thi.mynd.demoContent.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.demoContent.event.LoadedTopicsEvent;
import de.thi.mynd.demoContent.models.TopicModel;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
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
public final class TopicAssociationLoader {

    @Inject
    TopicAssociationRepository topicAssociationRepository;

    @Inject
    ObjectMapper mapper;

    @Transactional
    public void initializeAssociations(@Observes LoadedTopicsEvent event) throws IOException {
        if (topicAssociationRepository.count() > 0) {
            Log.info("Topic associations are already initialized");
            return;
        }

        Map<String, List<String>> associations = loadJson();

        for (var entry : associations.entrySet()) {
            Topic owning = event.mapping().get(entry.getKey());

            for (String foreignId : entry.getValue()) {
                TopicAssociation association = new TopicAssociation();
                association.creatorId = owning.creatorId;
                association.owningTopic = owning;
                association.foreignTopic = event.mapping().get(foreignId);

                topicAssociationRepository.persistAndFlush(association);
            }
        }

        topicAssociationRepository.flush();
        Log.info("Successfully initialized topic associations");

    }

    private Map<String, List<String>> loadJson() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("demo-content/topic-associations.json")) {
            return mapper.readValue(is, new TypeReference<Map<String, List<String>>>() {});
        }
    }
}
