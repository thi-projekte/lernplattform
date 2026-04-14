package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class CategoryResourceTest {

  @Inject CategoryRepository categoryRepository;

  @Test
  public void testSearch_whenAnonymous_thenIsUnauthorized() {
    given().when().get("/categories/search").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice")
  public void testSearch_whenNoParamProvided_thenReturnMaxOfFiveCategories() {
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

    Assertions.assertEquals(categoryRepository.findAllWithLimit(5), actual);
  }

  @Test
  @TestSecurity(user = "alice")
  public void testSearch_whenQueryProvided_thenFilterForTitle() {
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

    Assertions.assertEquals(categoryRepository.findByTitleWithLimit("techn", 5), actual);
  }

  @Test
  @TestSecurity(user = "alice")
  public void testSearch_whenQueryProvided_thenFilterForTitleLowercase() {
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

    Assertions.assertEquals(categoryRepository.findByTitleWithLimit("techn", 5), actual);
  }

  @Test
  @TestSecurity(user = "alice")
  public void testSearch_whenQueryProvided_thenFilterNotForColor() {
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
