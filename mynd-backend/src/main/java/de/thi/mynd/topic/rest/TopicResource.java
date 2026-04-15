package de.thi.mynd.topic.rest;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.service.TopicService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public final class TopicResource {

  @Inject TopicService topicService;

  @GET
  @Path("/personal")
  public PaginationDto<ListTopicDto> getPersonalTopicsPaginated(
      @RestQuery int page, @RestQuery int pageSize) {
    return topicService.findPersonalTopicsPaginated(page, pageSize);
  }

  @GET
  @Path("/search")
  public List<ListTopicDto> search(@RestQuery String search) {
    return topicService.findTopicsBySearchMax5(search);
  }
}
