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
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import de.thi.mynd.progressTracking.service.ChallengeService;

@ApplicationScoped
public final class LearnProgressServiceImpl implements LearnProgressService {

  @Inject LearnProgressTopicRepository learnProgressTopicRepository;

  @Inject LearnProgressContentElementRepository learnProgressContentElementRepository;

  @Inject SecurityIdentity identity;

  @Inject MappingRegistry mappingRegistry;

  @Inject ContentElementService contentElementService;

  @Inject StreakService streakService;

  @Inject ChallengeService challengeService;

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

    Log.infof("User %s successfully started learning topic %s", creatorId, topicId);

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

    streakService.continueOrStartStreaksForCurrentUser();

    Log.infof(
        "User %s successfully completed learning topic %s manually",
        identity.getPrincipal().getName(), topicId);
  }

  @Override
  @Transactional
  public void completeLearningContentElementAsCurrentUser(UUID contentElementId) {
    Optional<LearnProgressContentElement> learnProgressContentElement =
        getByContentElementIdAndCurrentCreator(contentElementId);

    if (learnProgressContentElement.isPresent() && learnProgressContentElement.get().completed) {
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
    String creatorId = identity.getPrincipal().getName();

    LearnProgressContentElementId id = new LearnProgressContentElementId();
    id.topicId = contentElement.topic.id;
    id.contentElementId = contentElementId;
    id.creatorId = creatorId;

    LearnProgressContentElement progressElement =
        learnProgressContentElement.orElse(new LearnProgressContentElement());
    progressElement.id = id;
    progressElement.completed = true;
    progressElement.progressTopic = progressTopic;

    progressTopic.contentElements.add(progressElement);

    if (progressTopic.contentElements.size() == progressTopic.contentElementsToComplete) {
      progressTopic.status = LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED;
      Log.infof("User %s successfully completed topic %s", creatorId, progressTopic.id.topicId);
    }

    learnProgressTopicRepository.persistAndFlush(progressTopic);

    streakService.continueOrStartStreaksForCurrentUser();
    challengeService.trackContentElementCompletion();
    Log.infof(
        "User %s successfully completed learning content element %s", creatorId, contentElementId);
  }

  @Override
  @Transactional
  public void resetTopicLearningProgress(UUID topicId) {
    Optional<LearnProgressTopic> topicOptional = getByTopicIdAndCurrentCreator(topicId);
    if (topicOptional.isEmpty()) {
      throw new TopicLearnProgressNotStartedException("This topic has not been started yet");
    }

    LearnProgressTopic progressTopic = topicOptional.get();
    progressTopic.status = LearnProgressStatus.STARTED;
    progressTopic.contentElements.forEach((ce) -> ce.completed = false);
    learnProgressTopicRepository.persistAndFlush(progressTopic);
    Log.infof(
        "User %s reset topic %s learning progress", identity.getPrincipal().getName(), topicId);
  }

  @Override
  @Transactional
  public void resetContentElementLearningProgress(UUID contentElementId) {
    Optional<LearnProgressContentElement> contentElementOptional =
        getByContentElementIdAndCurrentCreator(contentElementId);
    if (contentElementOptional.isEmpty()) {
      throw new TopicLearnProgressNotStartedException(
          "This content element has not been started yet");
    }

    LearnProgressContentElement progressContentElement = contentElementOptional.get();
    progressContentElement.completed = false;
    learnProgressContentElementRepository.persistAndFlush(progressContentElement);

    progressContentElement.progressTopic.status = LearnProgressStatus.STARTED;
    learnProgressTopicRepository.persistAndFlush(progressContentElement.progressTopic);

    Log.infof(
        "User %s reset content element %s learning progress",
        identity.getPrincipal().getName(), contentElementId);
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
