package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.request.CategoryRequest;
import de.thi.mynd.topic.service.CategoryServiceImpl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class CategoryResourceTest {

  @InjectMock CategoryRepository categoryRepository;

  @InjectMock
  CategoryServiceImpl categoryService;

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

    List<CategoryDto> actual =
        given()
            .when()
            .get("/categories/search")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", CategoryDto.class);

    Assertions.assertEquals(demo.title, actual.get(0).title);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterForTitle() {

    Category demo = new Category();
    demo.title = "Technology";
    List<Category> expected = Collections.singletonList(demo);

    Mockito.when(categoryRepository.findByTitleWithLimit("techn", 5)).thenReturn(expected);

    List<CategoryDto> actual =
        given()
            .when()
            .get("/categories/search?query=techn")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", CategoryDto.class);

    Assertions.assertEquals(demo.title, actual.get(0).title);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterForTitleLowercase() {

    Category demo = new Category();
    demo.title = "Technology";
    List<Category> expected = Collections.singletonList(demo);

    Mockito.when(categoryRepository.findByTitleWithLimit("TecHn", 5)).thenReturn(expected);

    List<CategoryDto> actual =
        given()
            .when()
            .get("/categories/search?query=TecHn")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", CategoryDto.class);

    Assertions.assertEquals(demo.title, actual.get(0).title);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterNotForColor() {

    Mockito.when(categoryRepository.findAllWithLimit(5)).thenReturn(Collections.emptyList());
    List<CategoryDto> actual =
        given()
            .when()
            .get("/categories/search?query=00CEC8")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .body()
            .jsonPath()
            .getList(".", CategoryDto.class);

    Assertions.assertSame(0, actual.size());
  }

  @Nested
  @DisplayName("GET /categories/tree")
  class GetFullTreeTests {

    @Test
    @TestSecurity(user = "admin", roles = {"authorizedUser", "admin"})
    @DisplayName("Should return full tree for admin user")
    void getFullTreeSuccess() {
      CategoryTreeDto treeDto = CategoryTreeDto.builder().build();
      Mockito.when(categoryService.getFullTree()).thenReturn(List.of(treeDto));

      given()
              .when().get("/categories/tree")
              .then()
              .statusCode(200)
              .contentType(ContentType.JSON)
              .body("$", hasSize(1));
    }
  }

  @Nested
  @DisplayName("POST /categories")
  class CreateCategoryTests {

    @Test
    @TestSecurity(user = "admin", roles = {"authorizedUser", "admin"})
    @DisplayName("Should successfully create a valid category")
    void createCategorySuccess() {
      CategoryRequest request = new CategoryRequest();
      // Assuming your request object has a setter for validation purposes, fill it here if needed

      Mockito.doNothing().when(categoryService).createCategory(any(CategoryRequest.class));

      given()
              .contentType(ContentType.JSON)
              .body(request)
              .when().post("/categories")
              .then()
              .statusCode(200);

      Mockito.verify(categoryService, Mockito.times(1)).createCategory(any(CategoryRequest.class));
    }
  }

  @Nested
  @DisplayName("PUT /categories/{categoryId}")
  class UpdateCategoryTests {

    @Test
    @TestSecurity(user = "admin", roles = {"authorizedUser", "admin"})
    @DisplayName("Should successfully update an existing category")
    void updateCategorySuccess() {
      UUID categoryId = UUID.randomUUID();
      CategoryRequest request = new CategoryRequest();

      Mockito.doNothing().when(categoryService).updateCategory(eq(categoryId), any(CategoryRequest.class));

      given()
              .pathParam("categoryId", categoryId.toString())
              .contentType(ContentType.JSON)
              .body(request)
              .when().put("/categories/{categoryId}")
              .then()
              .statusCode(200);

      Mockito.verify(categoryService, Mockito.times(1)).updateCategory(eq(categoryId), any(CategoryRequest.class));
    }
  }
}
