package de.thi.mynd.topic.rest;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.requests.CreateTopicRequest;
import de.thi.mynd.topic.service.ContentElementService;
import de.thi.mynd.topic.service.TopicService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public final class TopicResource {

  @Inject TopicService topicService;

  @Inject ContentElementService contentElementService;

  @GET
  @Path("/personal")
  public PaginationDto<ListTopicDto> getPersonalTopicsPaginated(
      @RestQuery int page, @RestQuery int pageSize) {
    return topicService.findPersonalTopicsPaginated(page, pageSize);
  }

  @GET
  public List<ListTopicDto> search(@RestQuery String search) {
    return topicService.findTopicsBySearchMax5(search);
  }

  @GET
  @Path("/{topicId}/content-elements")
  public List<ContentElementDto> getContentElementsForTopic(UUID topicId) {
    return contentElementService.getContentElementsForTopic(topicId);
  }

  @POST
  @RolesAllowed("builder")
  public TopicDto createTopic(@Valid CreateTopicRequest createTopicRequest) {
    return topicService.createTopic(createTopicRequest);
  }

  @DELETE
  @Path("/{topicId}")
  @RolesAllowed("builder")
  public Response deleteTopic(UUID topicId) {
    try {
      topicService.deleteTopic(topicId);
      return Response.ok().build();
    } catch (EntityInstanceNotFoundException e) {
      throw new NotFoundException(e.getMessage());
    }
  }
}
