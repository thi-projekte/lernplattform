/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.exception.CategoryNotFoundException;
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

  @InjectMock CategoryServiceImpl categoryService;

  @Test
  public void testSearch_whenAnonymous_thenIsUnauthorized() {
    given().when().get("/categories/search").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenNoParamProvided_thenReturnMaxOfFiveCategories() {

    CategoryDto demo = CategoryDto.builder().title("Technology").build();
    List<CategoryDto> expected = Collections.singletonList(demo);

    Mockito.when(categoryService.searchMax5(null)).thenReturn(expected);

    given()
        .when()
        .get("/categories/search")
        .then()
        .statusCode(200)
        .body("[0].title", is(demo.title));
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterForTitle() {

    CategoryDto demo = CategoryDto.builder().title("Technology").build();
    List<CategoryDto> expected = Collections.singletonList(demo);

    Mockito.when(categoryService.searchMax5("techn")).thenReturn(expected);

    given()
        .when()
        .get("/categories/search?query=techn")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON)
        .body("[0].title", is(demo.title));
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterForTitleLowercase() {

    CategoryDto demo = CategoryDto.builder().title("Technology").build();
    List<CategoryDto> expected = Collections.singletonList(demo);

    Mockito.when(categoryService.searchMax5("TecHn")).thenReturn(expected);

    given()
        .when()
        .get("/categories/search?query=TecHn")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON)
        .body("[0].title", is(demo.title));
  }

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  public void testSearch_whenQueryProvided_thenFilterNotForColor() {

    Mockito.when(categoryService.searchMax5(null)).thenReturn(Collections.emptyList());
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
    @TestSecurity(
        user = "admin",
        roles = {"authorizedUser", "admin"})
    @DisplayName("Should return full tree for admin user")
    void getFullTreeSuccess() {
      CategoryTreeDto treeDto = CategoryTreeDto.builder().build();
      Mockito.when(categoryService.getFullTree()).thenReturn(List.of(treeDto));

      given()
          .when()
          .get("/categories/tree")
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
    @TestSecurity(
        user = "admin",
        roles = {"authorizedUser", "admin"})
    @DisplayName("Should successfully create a valid category")
    void createCategorySuccess() {
      CategoryRequest request = new CategoryRequest();
      request.title = "new title";
      request.color = "111111";

      Mockito.doNothing().when(categoryService).createCategory(any(CategoryRequest.class));

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/categories")
          .then()
          .statusCode(200);

      Mockito.verify(categoryService, Mockito.times(1)).createCategory(any(CategoryRequest.class));
    }
  }

  @Nested
  @DisplayName("PUT /categories/{categoryId}")
  class UpdateCategoryTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"authorizedUser", "admin"})
    @DisplayName("Should successfully update an existing category")
    void updateCategorySuccess() {
      UUID categoryId = UUID.randomUUID();
      CategoryRequest request = new CategoryRequest();
      request.title = "new title";
      request.color = "111111";

      Mockito.doNothing()
          .when(categoryService)
          .updateCategory(eq(categoryId), any(CategoryRequest.class));

      given()
          .pathParam("categoryId", categoryId.toString())
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put("/categories/{categoryId}")
          .then()
          .statusCode(200);

      Mockito.verify(categoryService, Mockito.times(1))
          .updateCategory(eq(categoryId), any(CategoryRequest.class));
    }
  }

  @Test
  @TestSecurity(
      user = "adminUser",
      roles = {"admin"})
  void deleteCategory_AsAdmin_ShouldReturn200() {
    UUID categoryId = UUID.randomUUID();

    // Mocking the service to do nothing (successful deletion)
    Mockito.doNothing().when(categoryService).deleteCategory(categoryId);

    given()
        .pathParam("categoryId", categoryId)
        .when()
        .delete("/categories/{categoryId}") // Adjust path if you have a class-level @Path
        .then()
        .statusCode(200);

    Mockito.verify(categoryService, Mockito.times(1)).deleteCategory(categoryId);
  }

  @Test
  @TestSecurity(
      user = "adminUser",
      roles = {"admin"})
  void deleteCategory_WhenNotFound_ShouldReturn404() {
    UUID categoryId = UUID.randomUUID();

    // Mocking the service to throw your custom exception
    Mockito.doThrow(new CategoryNotFoundException("Category not found: " + categoryId))
        .when(categoryService)
        .deleteCategory(categoryId);

    given()
        .pathParam("categoryId", categoryId)
        .when()
        .delete("/categories/{categoryId}")
        .then()
        // Note: Assumes you have an ExceptionMapper that maps CategoryNotFoundException to 404.
        // If not, Quarkus defaults to 500 for unmapped custom exceptions.
        .statusCode(404);
  }

  @Test
  @TestSecurity(
      user = "regularUser",
      roles = {"user"})
  void deleteCategory_AsRegularUser_ShouldReturn403Forbidden() {
    UUID categoryId = UUID.randomUUID();

    given()
        .pathParam("categoryId", categoryId)
        .when()
        .delete("/categories/{categoryId}")
        .then()
        .statusCode(403); // Authenticated, but wrong role
  }

  @Test
  void deleteCategory_Anonymous_ShouldReturn401Unauthorized() {
    UUID categoryId = UUID.randomUUID();

    given()
        .pathParam("categoryId", categoryId)
        .when()
        .delete("/categories/{categoryId}")
        .then()
        .statusCode(401); // No credentials provided
  }
}
