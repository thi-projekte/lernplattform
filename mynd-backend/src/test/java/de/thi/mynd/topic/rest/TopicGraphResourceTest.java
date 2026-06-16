/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import de.thi.mynd.topic.service.TopicGraphService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TopicGraphResourceTest {

  @InjectMock TopicGraphService topicGraphService;

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"builder", "authorizedUser"})
  public void testGetNeighbors() {
    UUID topicId = UUID.randomUUID();

    when(topicGraphService.getNeighborsOfTopic(topicId))
        .thenReturn(List.of(GraphTopicDto.builder().build(), GraphTopicDto.builder().build()));

    given()
        .pathParam("topicId", topicId.toString())
        .when()
        .get("/topics/graph/{topicId}/neighbors")
        .then()
        .statusCode(200)
        .body("size()", is(2));
  }

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"builder", "authorizedUser"})
  public void testGetMostPopularNoCategoriesPersonalFails() {
    UUID topicId = UUID.randomUUID();

    when(topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(10, "alice"))
        .thenReturn(List.of());

    given()
        .when()
        .queryParam("builderMode", "true")
        .get("/topics/graph")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(0));
  }

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"builder", "authorizedUser"})
  public void testGetMostPopularNoCategoriesPersonalSuccess() {
    UUID topicId = UUID.randomUUID();
    GraphTopicDto dto = GraphTopicDto.builder().id(topicId).build();

    when(topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
            eq(100), eq("alice")))
        .thenReturn(List.of(dto));

    given()
        .when()
        .queryParam("builderMode", "true")
        .get("/topics/graph")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(1))
        .body("[0].id", is(topicId.toString()));
  }

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"builder", "authorizedUser"})
  public void testGetMostPopularWithCategoriesPersonalFails() {
    UUID categoryId = UUID.randomUUID();
    List<UUID> categories = List.of(categoryId);

    when(topicGraphService.getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
            eq(10), eq(categories), eq("alice")))
        .thenReturn(List.of());

    given()
        .queryParam("categories", categoryId.toString())
        .when()
        .queryParam("builderMode", "true")
        .get("/topics/graph")
        .then()
        .statusCode(200)
        .body("size()", is(0));
  }

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"builder", "authorizedUser"})
  public void testGetNeighborsPersonalFails() {
    UUID topicId = UUID.randomUUID();

    when(topicGraphService.getOwnedNeighborsOfTopic(topicId))
        .thenReturn(List.of(GraphTopicDto.builder().build()));

    given()
        .pathParam("topicId", topicId.toString())
        .when()
        .queryParam("builderMode", "true")
        .get("/topics/graph/{topicId}/neighbors")
        .then()
        .statusCode(200)
        .body("size()", is(1));
  }

  @Test
  @TestSecurity(
      user = "alice",
      roles = {"builder", "authorizedUser"})
  public void testGetNeighborsPersonalSuccess() {
    UUID topicId = UUID.randomUUID();

    when(topicGraphService.getOwnedNeighborsOfTopic(topicId))
        .thenReturn(List.of(GraphTopicDto.builder().build(), GraphTopicDto.builder().build()));

    given()
        .pathParam("topicId", topicId.toString())
        .when()
        .queryParam("builderMode", "true")
        .get("/topics/graph/{topicId}/neighbors")
        .then()
        .statusCode(200)
        .body("size()", is(2));
  }
}
