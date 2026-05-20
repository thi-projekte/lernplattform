package de.thi.mynd.topic.service;

import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.dto.loader.FullImportDto;
import de.thi.mynd.topic.dto.loader.ImportCategoryDto;
import de.thi.mynd.topic.dto.loader.ImportTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import de.thi.mynd.topic.exception.ImportException;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.security.TopicVoter;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class ImportServiceImpl implements ImportService {

    @Inject
    TopicRepository topicRepository;

    @Inject
    TopicAssociationRepository topicAssociationRepository;

    @Inject
    SecurityIdentity identity;

    @Inject
    SecurityService securityService;

    private Boolean isBackendMode;

    @Setter
    private Map<String, Topic> topicMapping;


    public void setBackendMode(Boolean backendMode) {
        this.isBackendMode = backendMode;
    }

    @Override
    public void importTopicJson(FullImportDto importDto) {

    }

    @Override
    public void importTopicJsonFromRequest(InputStream inputStream) {

    }

    @Override
    public void importCategories(List<ImportCategoryDto> categoryDtos) {

    }

    @Override
    public void importTopics(List<ImportTopicDto> topicDtos) {

    }

    @Override
    @Transactional
    public void importTopicAssociations(Map<String, List<String>> topicAssociations) {
        checkBackendMode();
        int count = 0;

        String creatorId = "admin";
        if (!isBackendMode) {
            creatorId = identity.getPrincipal().getName();
        }

        for (var entry : topicAssociations.entrySet()) {

            Topic owningTopic = getTopicFromMappingOrDatabase(entry.getKey());
            if (!isBackendMode) {
                securityService.denyUnlessGranted(owningTopic, TopicVoter.AssignForeignTopics);
            }

            for (String foreignId : entry.getValue()) {

                Topic foreignTopic = getTopicFromMappingOrDatabase(foreignId);
                if (topicAssociationRepository.associationExists(owningTopic, foreignTopic)) {
                    continue;
                }

                TopicAssociation association = new TopicAssociation();
                association.creatorId = creatorId;
                association.owningTopic = owningTopic;
                association.foreignTopic = foreignTopic;

                topicAssociationRepository.persist(association);
                count++;
            }
        }

        topicAssociationRepository.flush();

        Log.infof("User %s created %d new associations", creatorId, count);
        setBackendMode(null);
    }

    private void checkBackendMode() {
        if (isBackendMode == null) {
            throw new IllegalArgumentException("The backend mode needs to be explicitly set");
        }
    }

    private Topic getTopicFromMappingOrDatabase(String key) {
        if (topicMapping.containsKey(key)) {
            return topicMapping.get(key);
        }

        try {
            UUID topicId = UUID.fromString(key);
            Optional<Topic> topicOptional = topicRepository.findByIdOptional(topicId);

            if (topicOptional.isEmpty()) {
                throw new ImportException("Topic with ID " + key + " not found");
            }
            return topicOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ImportException("The topic ID " + key + " is not a valid UUID and the reference key does not exist either");
        }
    }
}
