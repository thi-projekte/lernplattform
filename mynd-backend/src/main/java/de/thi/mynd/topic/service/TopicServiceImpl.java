package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.requests.AssociatedContentElementRequest;
import de.thi.mynd.topic.requests.CreateTopicRequest;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public final class TopicServiceImpl implements TopicService {

  @Inject SecurityIdentity identity;

  @Inject MappingRegistry mappingRegistry;

  @Inject TopicRepository topicRepository;

  @Inject CategoryService categoryService;

  @Inject ContentElementService contentElementService;

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
  @Transactional
  public TopicDto createTopic(CreateTopicRequest request) {

    Topic topic = new Topic();
    topic.title = request.title;
    topic.creatorId = identity.getPrincipal().getName();
    topic.categories = categoryService.findByAssociatedEntities(request.categories);
    topic.relatedTopics =
        topicRepository.findByIdsTypeSafe(getIdsFromAssociatedEntities(request.relatedTopics));
    topic.estimatedLearningDuration = request.estimatedLearningDuration;
    topic.teaser = request.teaser;

    topicRepository.persist(topic);
    topicRepository.flush();

    updateContentAssignments(topic, request.contentElements);

    return mappingRegistry.map(topic, TopicDto.class);
  }

  private void updateContentAssignments(
      Topic topic, List<AssociatedContentElementRequest> newElements) {

    Set<UUID> incomingIds =
        newElements.stream().map(e -> e.id).filter(Objects::nonNull).collect(Collectors.toSet());

    List<ContentElement> elementsToRemove =
        topic.contentElements.stream().filter(e -> !incomingIds.contains(e.id)).toList();

    for (ContentElement element : elementsToRemove) {
      contentElementService.deleteContentElement(element.id);
    }

    contentElementService.updateTopicAssociation(topic, newElements);
  }

  private void filterContentElementsForCreator() {}

  private List<UUID> getIdsFromAssociatedEntities(List<AssociatedEntityRequest> entityRequests) {
    return entityRequests.stream().map((e) -> e.id).toList();
  }
}
