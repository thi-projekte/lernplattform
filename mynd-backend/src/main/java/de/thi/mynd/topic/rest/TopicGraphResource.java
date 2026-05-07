package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.service.TopicGraphService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/topics")
public final class TopicGraphResource {

  @Inject TopicGraphService topicGraphService;

  @Path("/most-popular")
  @GET
  public List<GraphTopicDto> getMostPopular(@RestQuery List<UUID> categories) {
    int n = 10;
    if (categories.isEmpty()) {
      return topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(n);
    }
    return topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(n, categories);
  }

  @Path("/{topicId}/graph-neighbors")
  @GET
  public List<GraphTopicDto> getNeighbors(UUID topicId) {
    return topicGraphService.getNeighborsOfTopic(topicId);
  }
}
