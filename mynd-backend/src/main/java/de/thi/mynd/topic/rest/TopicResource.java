package de.thi.mynd.topic.rest;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.requests.TopicRequest;
import de.thi.mynd.topic.service.TopicService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Topics")
@SecurityRequirement(name = "keycloak")
public final class TopicResource {

  @Inject TopicService topicService;

  @GET
  @Path("/personal")
  @RolesAllowed("builder")
  @Operation(
          summary = "Gets all the personal topics of the logged in builder",
          description = "Gets all the personal topics of the logged in builder in a paginated form")
  public PaginationDto<ListTopicDto> getPersonalTopicsPaginated(
      @RestQuery int page, @RestQuery int pageSize) {
    return topicService.findPersonalTopicsPaginated(page, pageSize);
  }

  @GET
  @Path("/{topicId}")
  @Operation(
          summary = "Finds a topic by topicId",
          description = "Finds a topic that is associated with the given topicId")
  @Parameter(name = "withOwnedRelatedTopics", description = "Indicates whether all the related topics that the topic owns should be provided too")
  @APIResponse(responseCode = "404", description = "The topic does not exist")
  public TopicDto getTopic(UUID topicId, @RestQuery boolean withOwnedRelatedTopics) {
      return topicService.getTopic(topicId, withOwnedRelatedTopics);
  }

  @GET
  @Operation(
          summary = "Searches for topics",
          description = "Searches a maximum of five topics based on the search string.")
  @Parameter(
          name = "search",
          description = "The search string that is used to find specific topics",
          required = true,
          example = "KI")
  public List<ListTopicDto> search(@RestQuery String search) {
    return topicService.findTopicsBySearchMax5(search);
  }

  @POST
  @RolesAllowed("builder")
  @Operation(
          summary = "Creates a new topic",
          description = "Creates a new topic and assigns the provided content elements to it.")
  public TopicDto createTopic(@Valid TopicRequest createTopicRequest) {
    return topicService.createTopic(createTopicRequest);
  }

  @PUT
  @Path("/{topicId}")
  @RolesAllowed("builder")
  @Operation(
          summary = "Updates a topic",
          description = "Updates a topic and updates the provided content elements by ID.")
  @APIResponse(responseCode = "404", description = "The topic does not exist")
  public TopicDto updateTopic(UUID topicId, @Valid TopicRequest updateTopicRequest) {
      return topicService.updateTopic(topicId, updateTopicRequest);
  }

  @DELETE
  @Path("/{topicId}")
  @RolesAllowed("builder")
  @Operation(
          summary = "Deletes a topic",
          description = "Deletes the topic with the given ID")
  @APIResponse(responseCode = "404", description = "The topic does not exist")
  public Response deleteTopic(UUID topicId) {
      topicService.deleteTopic(topicId);
      return Response.ok().build();
  }
}
