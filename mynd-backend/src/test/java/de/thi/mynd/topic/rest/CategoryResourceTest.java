package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class CategoryResourceTest {

  @InjectMock CategoryRepository categoryRepository;

  @Test
  public void testSearch_whenAnonymous_thenIsUnauthorized() {
    given().when().get("/categories/search").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenNoParamProvided_thenReturnMaxOfFiveCategories() {

    Category demo = new Category();
    demo.title = "Technology";
    List<Category> expected = Collections.singletonList(demo);

    Mockito.when(categoryRepository.findAllWithLimit(5)).thenReturn(expected);

    List<Category> actual =
        given()
            .when()
            .get("/categories/search")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Category.class);

    Assertions.assertEquals(demo.title, actual.get(0).title);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterForTitle() {

    Category demo = new Category();
    demo.title = "Technology";
    List<Category> expected = Collections.singletonList(demo);

    Mockito.when(categoryRepository.findByTitleWithLimit("techn", 5)).thenReturn(expected);

    List<Category> actual =
        given()
            .when()
            .get("/categories/search?query=techn")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Category.class);

    Assertions.assertEquals(demo.title, actual.get(0).title);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterForTitleLowercase() {

    Category demo = new Category();
    demo.title = "Technology";
    List<Category> expected = Collections.singletonList(demo);

    Mockito.when(categoryRepository.findByTitleWithLimit("TecHn", 5)).thenReturn(expected);

    List<Category> actual =
        given()
            .when()
            .get("/categories/search?query=TecHn")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Category.class);

    Assertions.assertEquals(demo.title, actual.get(0).title);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterNotForColor() {

    Mockito.when(categoryRepository.findAllWithLimit(5)).thenReturn(Collections.emptyList());
    List<Category> actual =
        given()
            .when()
            .get("/categories/search?query=00CEC8")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Category.class);

    Assertions.assertSame(0, actual.size());
  }
}
