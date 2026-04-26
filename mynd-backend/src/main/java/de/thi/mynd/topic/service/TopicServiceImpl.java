package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.dto.TopicWithOwnedRelatedTopicsDto;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.requests.TopicRequest;
import de.thi.mynd.topic.security.TopicVoter;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public final class TopicServiceImpl implements TopicService {

  @Inject SecurityIdentity identity;
  @Inject MappingRegistry mappingRegistry;
  @Inject TopicRepository topicRepository;
  @Inject CategoryService categoryService;
  @Inject ContentElementService contentElementService;
  @Inject TopicAssociationService topicAssociationService;
  @Inject SecurityService securityService;

  @Override
  public PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize) {
    PaginationDto<Topic> paginatedTopics =
        topicRepository.findForCreatorPaginated(identity.getPrincipal().getName(), page, pageSize);

    List<ListTopicDto> listDtos =
        mappingRegistry.mapList(paginatedTopics.results, ListTopicDto.class);

    return PaginationDto.<ListTopicDto>builder()
        .results(listDtos)
        .totalPages(paginatedTopics.totalPages)
        .build();
  }

  @Override
  public List<ListTopicDto> findTopicsBySearchMax5(String search) {
    List<Topic> topics = topicRepository.findBySearch(search, 5);
    return mappingRegistry.mapList(topics, ListTopicDto.class);
  }

  @Override
  public TopicDto getTopic(UUID topicId, boolean withOwnedRelatedTopics)
      throws EntityInstanceNotFoundException {
    Topic topic = getTopicByIdElseException(topicId);

    if (withOwnedRelatedTopics) {
      return mappingRegistry.map(topic, TopicWithOwnedRelatedTopicsDto.class);
    }

    return mappingRegistry.map(topic, TopicDto.class);
  }

  @Override
  @Transactional
  public TopicDto createTopic(TopicRequest request) {

    Topic topic = new Topic();
    updateTopicFieldsAndAssociations(topic, request);

    return mappingRegistry.map(topic, TopicDto.class);
  }

  @Override
  @Transactional
  public TopicDto updateTopic(UUID topicId, TopicRequest request)
      throws EntityInstanceNotFoundException {
    Topic topic = getTopicByIdElseException(topicId);

    securityService.denyUnlessGranted(topic, TopicVoter.Update);

    updateTopicFieldsAndAssociations(topic, request);

    return mappingRegistry.map(topic, TopicDto.class);
  }

  @Override
  @Transactional
  public void deleteTopic(UUID topicId) throws EntityInstanceNotFoundException {
    Topic topic = getTopicByIdElseException(topicId);

    securityService.denyUnlessGranted(topic, TopicVoter.Delete);

    for (ContentElement contentElement : topic.contentElements) {
      contentElementService.deleteContentElement(contentElement.id);
    }

    topicRepository.delete(topic);
    topicRepository.flush();
  }

  @Override
  public List<ListTopicDto> getOwnedRelatedTopicsForTopic(UUID topicId) {
    List<Topic> topics = topicRepository.findByOwningTopicId(topicId);

    return mappingRegistry.mapList(topics, ListTopicDto.class);
  }

  private void updateTopicFieldsAndAssociations(Topic topic, TopicRequest request) {
    topic.title = request.title;
    topic.teaser = request.teaser;
    topic.creatorId = identity.getPrincipal().getName();
    topic.categories = categoryService.findByAssociatedEntities(request.categories);
    topic.ownedAssociations.clear();
    topic.ownedAssociations.addAll(
        topicAssociationService.findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
            topic, request.relatedTopics, identity.getPrincipal().getName()));
    topic.estimatedLearningDuration = request.estimatedLearningDuration;

    topicRepository.persist(topic);
    topicRepository.flush();

    contentElementService.updateTopicAssociation(topic, request.contentElements);
  }

  private Topic getTopicByIdElseException(UUID topicId) throws EntityInstanceNotFoundException {
    Optional<Topic> topicOptional = topicRepository.findByIdOptional(topicId);
    if (topicOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("No topic exists for the provided UUID");
    }

    return topicOptional.get();
  }
}
