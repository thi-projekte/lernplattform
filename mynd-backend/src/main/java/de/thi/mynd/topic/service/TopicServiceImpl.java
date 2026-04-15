package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public final class TopicServiceImpl implements TopicService {

  @Inject SecurityIdentity identity;

  @Inject TopicRepository topicRepository;

  @Override
  public PaginationDto<ListTopicDto> findPersonalTopicsPaginated(int page, int pageSize) {
    PaginationDto<Topic> paginatedTopics =
        topicRepository.findForCreatorPaginated(identity.getPrincipal().getName(), page, pageSize);

    List<ListTopicDto> listDtos = paginatedTopics.results.stream().map(ListTopicDto::from).toList();

    return PaginationDto.<ListTopicDto>builder()
        .results(listDtos)
        .totalPages(paginatedTopics.totalPages)
        .hasNextPage(paginatedTopics.hasNextPage)
        .hasPreviousPage(paginatedTopics.hasPreviousPage)
        .build();
  }

  @Override
  public List<ListTopicDto> findTopicsBySearchMax5(String search) {
    return topicRepository.findBySearch(search, 5)
            .stream()
            .map(ListTopicDto::from)
            .toList();
  }
}
