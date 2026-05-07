package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.eq;

import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.service.TopicGraphService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class TopicGraphResourceTest {

  @InjectMock TopicGraphService topicGraphService;

  @Test
  @DisplayName("GET /topic/most-popular - Without categories should return top 10")
  public void testGetMostPopularNoCategories() {
    UUID topicId = UUID.randomUUID();
    GraphTopicDto dto = GraphTopicDto.builder().id(topicId).build();

    Mockito.when(topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(10))
        .thenReturn(List.of(dto));

    given()
        .when()
        .get("/topic/most-popular")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(1))
        .body("[0].id", is(topicId.toString()));
  }

  @Test
  @DisplayName("GET /topic/most-popular - With categories should filter results")
  public void testGetMostPopularWithCategories() {
    UUID categoryId = UUID.randomUUID();
    List<UUID> categories = List.of(categoryId);

    Mockito.when(
            topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
                eq(10), eq(categories)))
        .thenReturn(List.of(GraphTopicDto.builder().build()));

    given()
        .queryParam("categories", categoryId.toString())
        .when()
        .get("/topic/most-popular")
        .then()
        .statusCode(200)
        .body("size()", is(1));
  }

  @Test
  @DisplayName("GET /topic/{id}/graph-neighbors - Should return neighbor list")
  public void testGetNeighbors() {
    UUID topicId = UUID.randomUUID();

    Mockito.when(topicGraphService.getNeighborsOfTopic(topicId))
        .thenReturn(List.of(GraphTopicDto.builder().build(), GraphTopicDto.builder().build()));

    given()
        .pathParam("topicId", topicId.toString())
        .when()
        .get("/topic/{topicId}/graph-neighbors")
        .then()
        .statusCode(200)
        .body("size()", is(2));
  }
}
