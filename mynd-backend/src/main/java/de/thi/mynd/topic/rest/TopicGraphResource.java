package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.service.TopicGraphService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/topics/graph")
@Authenticated
public final class TopicGraphResource {

  @Inject TopicGraphService topicGraphService;
  @Inject SecurityIdentity securityIdentity;

  @Path("/most-popular")
  @GET
  public List<GraphTopicDto> getMostPopular(
      @RestQuery List<UUID> categories, @RestQuery Boolean personal) {
    if (categories.isEmpty()) {
      return personal
          ? topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
              100, securityIdentity.getPrincipal().getName())
          : topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(10);
    }
    return personal
        ? topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
            100, categories, securityIdentity.getPrincipal().getName())
        : topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(10, categories);
  }

  @Path("/{topicId}/neighbors")
  @GET
  public List<GraphTopicDto> getNeighbors(UUID topicId, @RestQuery Boolean personal) {
    return personal
        ? topicGraphService.getOwnedNeighborsOfTopic(topicId)
        : topicGraphService.getNeighborsOfTopic(topicId);
  }

  @GET
  public List<GraphTopicDto> searchTopic(@RestQuery String search) {
    return topicGraphService.searchTopicNodes(search, 5);
  }
}
