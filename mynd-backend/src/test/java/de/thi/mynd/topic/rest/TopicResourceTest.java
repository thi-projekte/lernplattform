package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.dto.TopicDto;
import de.thi.mynd.topic.requests.AssociatedContentElementRequest;
import de.thi.mynd.topic.requests.TopicRequest;
import de.thi.mynd.topic.service.TopicService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

@QuarkusTest
public class TopicResourceTest {

  @InjectMock TopicService topicService;

  @Test
  @TestSecurity(user = "alice")
  public void testGetPersonalTopicsSuccess() {
    ListTopicDto dto =
        ListTopicDto.builder()
            .title("Alice's Topic")
            .categories(new ArrayList<>())
            .updatedAt(LocalDateTime.now())
            .build();

    PaginationDto<ListTopicDto> mockResponse =
        PaginationDto.<ListTopicDto>builder().results(List.of(dto)).totalPages(1).build();

    when(topicService.findPersonalTopicsPaginated(anyInt(), anyInt())).thenReturn(mockResponse);

    given()
        .queryParam("page", 0)
        .queryParam("pageSize", 10)
        .when()
        .get("/topics/personal")
        .then()
        .statusCode(200)
        .body("results[0].title", is("Alice's Topic"))
        .body("totalPages", is(1));

    verify(topicService).findPersonalTopicsPaginated(0, 10);
  }

  @Test
  public void testGetPersonalTopicsUnauthorized() {
    given().when().get("/topics/personal").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "bob")
  public void testPaginationParameters() {
    given()
        .queryParam("page", 5)
        .queryParam("pageSize", 25)
        .when()
        .get("/topics/personal")
        .then()
        .statusCode(204);

    verify(topicService).findPersonalTopicsPaginated(5, 25);
  }

  @Test
  @TestSecurity(user = "alice")
  public void testSearchTopics() {
    ListTopicDto dto = ListTopicDto.builder().title("Found Topic").build();
    when(topicService.findTopicsBySearchMax5("search-term")).thenReturn(List.of(dto));

    given()
        .queryParam("search", "search-term")
        .when()
        .get("/topics")
        .then()
        .statusCode(200)
        .body("[0].title", is("Found Topic"));

    verify(topicService).findTopicsBySearchMax5("search-term");
  }

  @Test
  @TestSecurity(user = "builder-user", roles = "builder")
  public void testCreateTopicAsBuilderWithInvalidRequestBody() {
    TopicRequest request = new TopicRequest();
    request.title = "New Title";

    TopicDto responseDto = TopicDto.builder().title("New Title").build();

    when(topicService.createTopic(ArgumentMatchers.any())).thenReturn(responseDto);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/topics")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "builder-user", roles = "builder")
  public void testCreateTopicAsBuilderWithValidRequestBody() {

    AssociatedContentElementRequest contentElementRequest = new AssociatedContentElementRequest();
    contentElementRequest.id = UUID.randomUUID();

    AssociatedEntityRequest category = new AssociatedEntityRequest();
    category.id = UUID.randomUUID();

    AssociatedEntityRequest topic = new AssociatedEntityRequest();
    topic.id = UUID.randomUUID();

    TopicRequest request = new TopicRequest();
    request.title = "New Title";
    request.teaser = "Teaser";
    request.relatedTopics = List.of(topic);
    request.categories = List.of(category);
    request.estimatedLearningDuration = 12;
    request.contentElements = List.of(contentElementRequest);

    TopicDto responseDto = TopicDto.builder().title("New Title").build();

    when(topicService.createTopic(ArgumentMatchers.any())).thenReturn(responseDto);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/topics")
        .then()
        .statusCode(200)
        .body("title", is("New Title"));

    verify(topicService).createTopic(ArgumentMatchers.any());
  }

  @Test
  @TestSecurity(user = "regular-user", roles = "user")
  public void testCreateTopicForbiddenForRegularUser() {
    TopicRequest request = new TopicRequest();
    request.title = "Forbidden Title";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/topics")
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "builder-user", roles = "builder")
  public void testDeleteTopic() {

    given().when().delete("/topics/" + UUID.randomUUID()).then().statusCode(200);
  }
}
