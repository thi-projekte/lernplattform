package de.thi.mynd.progressTracking.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.entity.*;
import de.thi.mynd.progressTracking.exception.ContentElementLearnProgressAlreadyCompletedException;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressAlreadyStartedException;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressNotStartedException;
import de.thi.mynd.progressTracking.repository.LearnProgressContentElementRepository;
import de.thi.mynd.progressTracking.repository.LearnProgressTopicRepository;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.service.ContentElementService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public final class LearnProgressServiceImpl implements LearnProgressService {

  @Inject LearnProgressTopicRepository learnProgressTopicRepository;

  @Inject LearnProgressContentElementRepository learnProgressContentElementRepository;

  @Inject SecurityIdentity identity;

  @Inject MappingRegistry mappingRegistry;

  @Inject ContentElementService contentElementService;

  @Override
  public Map<UUID, TopicLearnProgressDto> getLearnProgressMappingForTopics(List<UUID> topicIds) {
    String creatorId = identity.getPrincipal().getName();
    List<LearnProgressTopic> topics =
        learnProgressTopicRepository.findByTopicIdsAndCreatorIdContentElementsFetched(
            topicIds, creatorId);

    return topics.stream()
        .collect(
            Collectors.toMap(
                topic -> topic.id.topicId,
                topic -> mappingRegistry.map(topic, TopicLearnProgressDto.class)));
  }

  @Override
  public TopicLearnProgressDto getLearnProgressForTopic(UUID topicId) {
    String creatorId = identity.getPrincipal().getName();
    Optional<LearnProgressTopic> progressTopicOptional =
        learnProgressTopicRepository.findOneByTopicIdAndCreatorIdContentElementsFetched(
            topicId, creatorId);

    if (progressTopicOptional.isEmpty()) {
      throw new TopicLearnProgressNotStartedException("This topic has not been started yet");
    }

    return mappingRegistry.map(progressTopicOptional.get(), TopicLearnProgressDto.class);
  }

  @Override
  @Transactional
  public void startLearningTopicAsCurrentUser(UUID topicId) {
    Optional<LearnProgressTopic> learnProgressTopicOptional =
        getByTopicIdAndCurrentCreator(topicId);

    if (learnProgressTopicOptional.isPresent()) {
      throw new TopicLearnProgressAlreadyStartedException("This topic has already been started");
    }

    String creatorId = identity.getPrincipal().getName();

    LearnProgressTopicId id = new LearnProgressTopicId();
    id.topicId = topicId;
    id.creatorId = creatorId;

    long contentElementCount = contentElementService.getCountOfElementsForTopicId(topicId);

    LearnProgressTopic progressTopic = new LearnProgressTopic();
    progressTopic.id = id;
    progressTopic.contentElementsToComplete = contentElementCount;
    progressTopic.status = LearnProgressStatus.STARTED;

    learnProgressTopicRepository.persistAndFlush(progressTopic);
  }

  @Override
  @Transactional
  public void manuallyCompleteTopicAsCurrentUser(UUID topicId) {
    Optional<LearnProgressTopic> learnProgressTopicOptional =
        getByTopicIdAndCurrentCreator(topicId);

    if (learnProgressTopicOptional.isEmpty()) {
      throw new TopicLearnProgressNotStartedException("This topic has not been started yet");
    }

    LearnProgressTopic learnProgressTopic = learnProgressTopicOptional.get();
    learnProgressTopic.status = LearnProgressStatus.COMPLETED_MANUALLY;
    learnProgressTopicRepository.persistAndFlush(learnProgressTopic);
  }

  @Override
  @Transactional
  public void completeLearningContentElementAsCurrentUser(UUID contentElementId) {
    Optional<LearnProgressContentElement> learnProgressContentElement =
        getByContentElementIdAndCurrentCreator(contentElementId);

    if (learnProgressContentElement.isPresent()) {
      throw new ContentElementLearnProgressAlreadyCompletedException(
          "Content element already completed");
    }

    ContentElement contentElement =
        contentElementService.getContentElementEntityById(contentElementId);

    Optional<LearnProgressTopic> learnProgressTopicOptional =
        getByTopicIdAndCurrentCreator(contentElement.topic.id);
    if (learnProgressTopicOptional.isEmpty()) {
      throw new TopicLearnProgressNotStartedException("This topic has not been started yet");
    }

    LearnProgressTopic progressTopic = learnProgressTopicOptional.get();

    LearnProgressContentElementId id = new LearnProgressContentElementId();
    id.topicId = contentElement.topic.id;
    id.contentElementId = contentElementId;
    id.creatorId = identity.getPrincipal().getName();

    LearnProgressContentElement progressElement = new LearnProgressContentElement();
    progressElement.id = id;
    progressElement.completed = true;
    progressElement.progressTopic = progressTopic;

    progressTopic.contentElements.add(progressElement);

    if (progressTopic.contentElements.size() == progressTopic.contentElementsToComplete) {
      progressTopic.status = LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED;
    }

    learnProgressTopicRepository.persistAndFlush(progressTopic);
  }

  private Optional<LearnProgressTopic> getByTopicIdAndCurrentCreator(UUID topicId) {
    String creatorId = identity.getPrincipal().getName();
    LearnProgressTopicId id = new LearnProgressTopicId();
    id.topicId = topicId;
    id.creatorId = creatorId;
    return learnProgressTopicRepository.findByIdOptional(id);
  }

  private Optional<LearnProgressContentElement> getByContentElementIdAndCurrentCreator(
      UUID contentElementId) {
    String creatorId = identity.getPrincipal().getName();
    return learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
        contentElementId, creatorId);
  }
}
