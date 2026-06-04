package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.service.TopicGraphService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/topics/graph")
@RolesAllowed("authorizedUser")
@Tag(name = "Topics Graph")
@SecurityRequirement(name = "keycloak")
public final class TopicGraphResource {

  @Inject TopicGraphService topicGraphService;
  @Inject SecurityIdentity securityIdentity;

  @GET
  @Operation(
      summary = "Get most popular topics",
      description =
          "Returns the most popular topics in the graph along with their direct neighbors. "
              + "Without filters returns the top 10 globally, or top 100 when personalized for the current user.")
  @Parameter(name = "builderMode", description = "If true, results are for the current builder")
  @APIResponse(
      responseCode = "200",
      description = "List of popular topics with their direct neighbors")
  public List<GraphTopicDto> getMostPopular(@RestQuery @DefaultValue("false") boolean builderMode) {
    return builderMode
        ? topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
            100, securityIdentity.getPrincipal().getName())
        : topicGraphService.getLearnGraph();
  }

  @Path("/{topicId}/neighbors")
  @GET
  @Operation(
      summary = "Get neighbors of a topic",
      description =
          "Returns all direct neighbors of the given topic in the graph. "
              + "When personalized, only returns neighbors owned by the current user.")
  @Parameter(
      name = "topicId",
      description = "The unique ID of the topic whose neighbors should be retrieved.")
  @Parameter(
      name = "personal",
      description = "If true, only neighbors owned by the current user are returned.")
  @APIResponse(responseCode = "200", description = "List of neighboring topics")
  @APIResponse(responseCode = "404", description = "Topic not found")
  public List<GraphTopicDto> getNeighbors(
      UUID topicId, @RestQuery @DefaultValue("false") boolean personal) {
    return personal
        ? topicGraphService.getOwnedNeighborsOfTopic(topicId)
        : topicGraphService.getNeighborsOfTopic(topicId);
  }

  @GET
  @Path("/search")
  @Operation(
      summary = "Search topics",
      description = "Searches for topics in the graph by name. Returns up to 5 matching results.")
  @Parameter(name = "search", description = "The search term to match against topic names.")
  @APIResponse(responseCode = "200", description = "List of matching topics")
  public List<GraphTopicDto> searchTopic(@RestQuery String search) {
    return topicGraphService.searchTopicNodes(search, 5);
  }
}
