package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.service.TopicService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        PaginationDto.<ListTopicDto>builder()
            .results(List.of(dto))
            .totalPages(1)
            .hasNextPage(false)
            .hasPreviousPage(false)
            .build();

    Mockito.when(topicService.findPersonalTopicsPaginated(anyInt(), anyInt()))
        .thenReturn(mockResponse);

    given()
        .queryParam("page", 0)
        .queryParam("pageSize", 10)
        .when()
        .get("/topics/personal")
        .then()
        .statusCode(200)
        .body("results[0].title", is("Alice's Topic"))
        .body("totalPages", is(1));

    Mockito.verify(topicService).findPersonalTopicsPaginated(0, 10);
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

    Mockito.verify(topicService).findPersonalTopicsPaginated(5, 25);
  }
}
