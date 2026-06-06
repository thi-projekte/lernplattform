package de.thi.mynd.progressTracking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.entity.*;
import de.thi.mynd.progressTracking.exception.ContentElementLearnProgressAlreadyCompletedException;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressAlreadyStartedException;
import de.thi.mynd.progressTracking.exception.TopicLearnProgressNotStartedException;
import de.thi.mynd.progressTracking.repository.LearnProgressContentElementRepository;
import de.thi.mynd.progressTracking.repository.LearnProgressTopicRepository;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.PdfElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.service.ContentElementService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LearnProgressServiceImplTest {

  @Inject LearnProgressService learnProgressService;

  @InjectMock LearnProgressTopicRepository learnProgressTopicRepository;
  @InjectMock LearnProgressContentElementRepository learnProgressContentElementRepository;
  @InjectMock SecurityIdentity identity;
  @InjectMock MappingRegistry mappingRegistry;
  @InjectMock ContentElementService contentElementService;

  private static final String CREATOR_ID = "user-123";
  private static final UUID TOPIC_ID = UUID.randomUUID();
  private static final UUID CONTENT_ELEMENT_ID = UUID.randomUUID();

  @BeforeEach
  void setupIdentity() {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(CREATOR_ID);
    when(identity.getPrincipal()).thenReturn(principal);
  }

  // ---------------------------------------------------------------------------
  // getLearnProgressForTopic
  // ---------------------------------------------------------------------------

  @Test
  void getLearnProgressForTopic_returnsDto_whenTopicStarted() {
    UUID topicId = UUID.randomUUID();
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 3);
    TopicLearnProgressDto expectedDto = TopicLearnProgressDto.builder().build();

    when(learnProgressTopicRepository.findOneByTopicIdAndCreatorIdContentElementsFetched(
            topicId, CREATOR_ID))
        .thenReturn(Optional.of(progressTopic));
    when(mappingRegistry.map(progressTopic, TopicLearnProgressDto.class)).thenReturn(expectedDto);

    TopicLearnProgressDto result = learnProgressService.getLearnProgressForTopic(topicId);

    assertNotNull(result);
    assertEquals(expectedDto, result);
  }

  @Test
  void getLearnProgressForTopic_throws_whenTopicNotStarted() {
    UUID topicId = UUID.randomUUID();

    when(learnProgressTopicRepository.findOneByTopicIdAndCreatorIdContentElementsFetched(
            topicId, CREATOR_ID))
        .thenReturn(Optional.empty());

    assertThrows(
        TopicLearnProgressNotStartedException.class,
        () -> learnProgressService.getLearnProgressForTopic(topicId));
  }

  // ---------------------------------------------------------------------------
  // getLearnProgressMappingForTopics
  // ---------------------------------------------------------------------------

  @Test
  void getLearnProgressMappingForTopics_returnsMapKeyedByTopicId() {
    UUID topicId1 = UUID.randomUUID();
    UUID topicId2 = UUID.randomUUID();
    LearnProgressTopic t1 = buildProgressTopic(topicId1, LearnProgressStatus.STARTED, 2);
    LearnProgressTopic t2 = buildProgressTopic(topicId2, LearnProgressStatus.COMPLETED_MANUALLY, 2);
    TopicLearnProgressDto dto1 = TopicLearnProgressDto.builder().build();
    TopicLearnProgressDto dto2 = TopicLearnProgressDto.builder().build();

    when(learnProgressTopicRepository.findByTopicIdsAndCreatorIdContentElementsFetched(
            List.of(topicId1, topicId2), CREATOR_ID))
        .thenReturn(List.of(t1, t2));
    when(mappingRegistry.map(t1, TopicLearnProgressDto.class)).thenReturn(dto1);
    when(mappingRegistry.map(t2, TopicLearnProgressDto.class)).thenReturn(dto2);

    Map<UUID, TopicLearnProgressDto> result =
        learnProgressService.getLearnProgressMappingForTopics(List.of(topicId1, topicId2));

    assertEquals(2, result.size());
    assertEquals(dto1, result.get(topicId1));
    assertEquals(dto2, result.get(topicId2));
  }

  @Test
  void getLearnProgressMappingForTopics_returnsEmptyMap_whenNoProgressExists() {
    when(learnProgressTopicRepository.findByTopicIdsAndCreatorIdContentElementsFetched(
            List.of(), CREATOR_ID))
        .thenReturn(List.of());

    Map<UUID, TopicLearnProgressDto> result =
        learnProgressService.getLearnProgressMappingForTopics(List.of());

    assertTrue(result.isEmpty());
  }

  // ---------------------------------------------------------------------------
  // startLearningTopicAsCurrentUser
  // ---------------------------------------------------------------------------

  @Test
  void startLearningTopicAsCurrentUser_persistsTopic_whenNotYetStarted() {
    UUID topicId = UUID.randomUUID();

    when(learnProgressTopicRepository.findByIdOptional(any())).thenReturn(Optional.empty());
    when(contentElementService.getCountOfElementsForTopicId(topicId)).thenReturn(5L);

    learnProgressService.startLearningTopicAsCurrentUser(topicId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(
            argThat(
                topic ->
                    topic.id.topicId.equals(topicId)
                        && topic.id.creatorId.equals(CREATOR_ID)
                        && topic.status == LearnProgressStatus.STARTED
                        && topic.contentElementsToComplete == 5L));
  }

  @Test
  void startLearningTopicAsCurrentUser_throws_whenAlreadyStarted() {
    UUID topicId = UUID.randomUUID();
    LearnProgressTopic existing = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 3);

    when(learnProgressTopicRepository.findByIdOptional(any())).thenReturn(Optional.of(existing));

    assertThrows(
        TopicLearnProgressAlreadyStartedException.class,
        () -> learnProgressService.startLearningTopicAsCurrentUser(topicId));

    verify(learnProgressTopicRepository, never()).persistAndFlush(any());
  }

  @Test
  void startLearningTopicAsCurrentUser_setsContentElementCount_fromService() {
    UUID topicId = UUID.randomUUID();

    when(learnProgressTopicRepository.findByIdOptional(any())).thenReturn(Optional.empty());
    when(contentElementService.getCountOfElementsForTopicId(topicId)).thenReturn(0L);

    learnProgressService.startLearningTopicAsCurrentUser(topicId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(argThat(topic -> topic.contentElementsToComplete == 0L));
  }

  // ---------------------------------------------------------------------------
  // manuallyCompleteTopicAsCurrentUser
  // ---------------------------------------------------------------------------

  @Test
  void manuallyCompleteTopicAsCurrentUser_setsStatus_whenTopicStarted() {
    UUID topicId = UUID.randomUUID();
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 3);

    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.manuallyCompleteTopicAsCurrentUser(topicId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(argThat(topic -> topic.status == LearnProgressStatus.COMPLETED_MANUALLY));
  }

  @Test
  void manuallyCompleteTopicAsCurrentUser_throws_whenTopicNotStarted() {
    UUID topicId = UUID.randomUUID();

    when(learnProgressTopicRepository.findByIdOptional(any())).thenReturn(Optional.empty());

    assertThrows(
        TopicLearnProgressNotStartedException.class,
        () -> learnProgressService.manuallyCompleteTopicAsCurrentUser(topicId));

    verify(learnProgressTopicRepository, never()).persistAndFlush(any());
  }

  @Test
  void manuallyCompleteTopicAsCurrentUser_canCompleteAlreadyAutoCompleted() {
    UUID topicId = UUID.randomUUID();
    LearnProgressTopic progressTopic =
        buildProgressTopic(topicId, LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED, 2);

    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.manuallyCompleteTopicAsCurrentUser(topicId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(argThat(topic -> topic.status == LearnProgressStatus.COMPLETED_MANUALLY));
  }

  // ---------------------------------------------------------------------------
  // completeLearningContentElementAsCurrentUser
  // ---------------------------------------------------------------------------

  @Test
  void completeContentElement_throws_whenAlreadyCompleted() {
    UUID contentElementId = UUID.randomUUID();
    LearnProgressContentElement existing = new LearnProgressContentElement();
    existing.completed = true;

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.of(existing));

    assertThrows(
        ContentElementLearnProgressAlreadyCompletedException.class,
        () -> learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId));

    verify(learnProgressTopicRepository, never()).persistAndFlush(any());
  }

  @Test
  void completeContentElement_doesNotThrow_whenAlreadyCompleted() {
    UUID contentElementId = UUID.randomUUID();
    UUID topicId = UUID.randomUUID();
    LearnProgressContentElement existing = new LearnProgressContentElement();
    existing.completed = false;
    ContentElement contentElement = buildContentElement(contentElementId, topicId);
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 3);

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.of(existing));
    when(contentElementService.getContentElementEntityById(contentElementId))
        .thenReturn(contentElement);
    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    assertDoesNotThrow(
        () -> learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId));
  }

  @Test
  void completeContentElement_throws_whenTopicNotStarted() {
    UUID contentElementId = UUID.randomUUID();
    UUID topicId = UUID.randomUUID();
    ContentElement contentElement = buildContentElement(contentElementId, topicId);

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.empty());
    when(contentElementService.getContentElementEntityById(contentElementId))
        .thenReturn(contentElement);
    when(learnProgressTopicRepository.findByIdOptional(any())).thenReturn(Optional.empty());

    assertThrows(
        TopicLearnProgressNotStartedException.class,
        () -> learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId));
  }

  @Test
  void completeContentElement_addsElementAndPersists_whenTopicStarted() {
    UUID contentElementId = UUID.randomUUID();
    UUID topicId = UUID.randomUUID();
    ContentElement contentElement = buildContentElement(contentElementId, topicId);
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 3);

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.empty());
    when(contentElementService.getContentElementEntityById(contentElementId))
        .thenReturn(contentElement);
    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(
            argThat(
                topic ->
                    topic.contentElements.size() == 1
                        && topic.contentElements.get(0).id.contentElementId.equals(contentElementId)
                        && topic.contentElements.get(0).completed));
  }

  @Test
  void completeContentElement_setsStatusToAllCompleted_whenLastElementCompleted() {
    UUID contentElementId = UUID.randomUUID();
    UUID topicId = UUID.randomUUID();
    ContentElement contentElement = buildContentElement(contentElementId, topicId);
    // contentElementsToComplete = 1, contentElements is empty → completing this is the last one
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 1);

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.empty());
    when(contentElementService.getContentElementEntityById(contentElementId))
        .thenReturn(contentElement);
    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(
            argThat(topic -> topic.status == LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED));
  }

  @Test
  void completeContentElement_doesNotSetAllCompleted_whenMoreElementsRemain() {
    UUID contentElementId = UUID.randomUUID();
    UUID topicId = UUID.randomUUID();
    ContentElement contentElement = buildContentElement(contentElementId, topicId);
    // 2 elements needed, completing only the 1st
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 2);

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.empty());
    when(contentElementService.getContentElementEntityById(contentElementId))
        .thenReturn(contentElement);
    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(argThat(topic -> topic.status == LearnProgressStatus.STARTED));
  }

  @Test
  void completeContentElement_assignsCorrectCreatorId() {
    UUID contentElementId = UUID.randomUUID();
    UUID topicId = UUID.randomUUID();
    ContentElement contentElement = buildContentElement(contentElementId, topicId);
    LearnProgressTopic progressTopic = buildProgressTopic(topicId, LearnProgressStatus.STARTED, 5);

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            contentElementId, CREATOR_ID))
        .thenReturn(Optional.empty());
    when(contentElementService.getContentElementEntityById(contentElementId))
        .thenReturn(contentElement);
    when(learnProgressTopicRepository.findByIdOptional(any()))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId);

    verify(learnProgressTopicRepository)
        .persistAndFlush(
            argThat(topic -> topic.contentElements.get(0).id.creatorId.equals(CREATOR_ID)));
  }

  @Test
  void resetTopicLearningProgress_shouldResetStatusAndContentElements() {
    // Arrange
    LearnProgressContentElement ce1 = new LearnProgressContentElement();
    ce1.completed = true;
    LearnProgressContentElement ce2 = new LearnProgressContentElement();
    ce2.completed = true;

    LearnProgressTopic progressTopic = new LearnProgressTopic();
    progressTopic.status = LearnProgressStatus.COMPLETED_MANUALLY;
    progressTopic.contentElements = List.of(ce1, ce2);

    LearnProgressTopicId expectedId = new LearnProgressTopicId();
    expectedId.topicId = TOPIC_ID;
    expectedId.creatorId = CREATOR_ID;

    when(learnProgressTopicRepository.findByIdOptional(any(LearnProgressTopicId.class)))
        .thenReturn(Optional.of(progressTopic));

    // Act
    learnProgressService.resetTopicLearningProgress(TOPIC_ID);

    // Assert
    assertEquals(LearnProgressStatus.STARTED, progressTopic.status);
    assertFalse(ce1.completed);
    assertFalse(ce2.completed);
    verify(learnProgressTopicRepository).persistAndFlush(progressTopic);
  }

  @Test
  void resetTopicLearningProgress_shouldThrow_whenProgressNotFound() {
    when(learnProgressTopicRepository.findByIdOptional(any(LearnProgressTopicId.class)))
        .thenReturn(Optional.empty());

    assertThrows(
        TopicLearnProgressNotStartedException.class,
        () -> learnProgressService.resetTopicLearningProgress(TOPIC_ID));

    verify(learnProgressTopicRepository, never()).persistAndFlush(any());
  }

  @Test
  void resetTopicLearningProgress_shouldWork_whenNoContentElements() {
    LearnProgressTopic progressTopic = new LearnProgressTopic();
    progressTopic.status = LearnProgressStatus.COMPLETED_MANUALLY;
    progressTopic.contentElements = Collections.emptyList();

    when(learnProgressTopicRepository.findByIdOptional(any(LearnProgressTopicId.class)))
        .thenReturn(Optional.of(progressTopic));

    learnProgressService.resetTopicLearningProgress(TOPIC_ID);

    assertEquals(LearnProgressStatus.STARTED, progressTopic.status);
    verify(learnProgressTopicRepository).persistAndFlush(progressTopic);
  }

  // ── resetContentElementLearningProgress ────────────────────────────────

  @Test
  void resetContentElementLearningProgress_shouldResetElementAndTopicStatus() {
    // Arrange
    LearnProgressTopic progressTopic = new LearnProgressTopic();
    progressTopic.status = LearnProgressStatus.COMPLETED_MANUALLY;

    LearnProgressContentElement progressCE = new LearnProgressContentElement();
    progressCE.completed = true;
    progressCE.progressTopic = progressTopic;

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            CONTENT_ELEMENT_ID, CREATOR_ID))
        .thenReturn(Optional.of(progressCE));

    // Act
    learnProgressService.resetContentElementLearningProgress(CONTENT_ELEMENT_ID);

    // Assert
    assertFalse(progressCE.completed);
    assertEquals(LearnProgressStatus.STARTED, progressTopic.status);
    verify(learnProgressContentElementRepository).persistAndFlush(progressCE);
    verify(learnProgressTopicRepository).persistAndFlush(progressTopic);
  }

  @Test
  void resetContentElementLearningProgress_shouldThrow_whenProgressNotFound() {
    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            CONTENT_ELEMENT_ID, CREATOR_ID))
        .thenReturn(Optional.empty());

    assertThrows(
        TopicLearnProgressNotStartedException.class,
        () -> learnProgressService.resetContentElementLearningProgress(CONTENT_ELEMENT_ID));

    verify(learnProgressContentElementRepository, never()).persistAndFlush(any());
    verify(learnProgressTopicRepository, never()).persistAndFlush(any());
  }

  @Test
  void resetContentElementLearningProgress_shouldPersistBothRepositories() {
    LearnProgressTopic progressTopic = new LearnProgressTopic();
    progressTopic.status = LearnProgressStatus.STARTED; // already STARTED

    LearnProgressContentElement progressCE = new LearnProgressContentElement();
    progressCE.completed = true;
    progressCE.progressTopic = progressTopic;

    when(learnProgressContentElementRepository.findByContentElementIdAndCreatorId(
            CONTENT_ELEMENT_ID, CREATOR_ID))
        .thenReturn(Optional.of(progressCE));

    learnProgressService.resetContentElementLearningProgress(CONTENT_ELEMENT_ID);

    // Both repos must be flushed regardless of prior topic status
    verify(learnProgressContentElementRepository, times(1)).persistAndFlush(progressCE);
    verify(learnProgressTopicRepository, times(1)).persistAndFlush(progressTopic);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private LearnProgressTopic buildProgressTopic(
      UUID topicId, LearnProgressStatus status, long elementsToComplete) {
    LearnProgressTopicId id = new LearnProgressTopicId();
    id.topicId = topicId;
    id.creatorId = CREATOR_ID;

    LearnProgressTopic topic = new LearnProgressTopic();
    topic.id = id;
    topic.status = status;
    topic.contentElementsToComplete = elementsToComplete;
    topic.contentElements = new ArrayList<>();
    return topic;
  }

  private ContentElement buildContentElement(UUID contentElementId, UUID topicId) {
    Topic topic = new Topic();
    topic.id = topicId;

    ContentElement element = new PdfElement();
    element.id = contentElementId;
    element.topic = topic;
    return element;
  }
}
