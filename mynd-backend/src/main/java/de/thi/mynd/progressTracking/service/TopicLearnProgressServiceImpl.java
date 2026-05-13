package de.thi.mynd.progressTracking.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressNotStartedException;
import de.thi.mynd.progressTracking.repository.LearnProgressTopicRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public final class TopicLearnProgressServiceImpl implements TopicLearnProgressService {

    @Inject
    LearnProgressTopicRepository learnProgressTopicRepository;

    @Inject
    SecurityIdentity identity;

    @Inject
    MappingRegistry mappingRegistry;

    @Override
    public Map<UUID, TopicLearnProgressDto> getLearnProgressMappingForTopics(List<UUID> topicIds) {
        String creatorId = identity.getPrincipal().getName();
        List<LearnProgressTopic> topics = learnProgressTopicRepository.findByTopicIdsAndCreatorIdContentElementsFetched(topicIds, creatorId);

        return topics.stream()
                .collect(Collectors.toMap(
                        topic -> topic.id.topicId,
                        topic -> mappingRegistry.map(topic, TopicLearnProgressDto.class)
                ));
    }

    @Override
    public TopicLearnProgressDto getLearnProgressForTopic(UUID topicId) {
        String creatorId = identity.getPrincipal().getName();
        Optional<LearnProgressTopic> progressTopicOptional = learnProgressTopicRepository.findOneByTopicIdAndCreatorIdContentElementsFetched(topicId, creatorId);

        if (progressTopicOptional.isEmpty()) {
            throw new TopicLearnProgressNotStartedException("This topic has not been started yet");
        }

        return mappingRegistry.map(progressTopicOptional.get(), TopicLearnProgressDto.class);
    }

    @Override
    public void startLearningTopicAsCurrentUser(UUID topicId) {

    }

    @Override
    public void manuallyCompleteTopicAsCurrentUser(UUID topicId) {

    }

    @Override
    public void completeLearningContentElementAsCurrentUser(UUID contentElementId) {

    }
}
