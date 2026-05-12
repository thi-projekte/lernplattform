package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.service.CategoryService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Categories")
@SecurityRequirement(name = "keycloak")
public final class CategoryResource {

  @Inject CategoryService categoryService;

  @Path("/search")
  @GET
  @Operation(
      summary = "Searches for categories",
      description = "Searches a maximum of five categories based on the search string.")
  @Parameter(
      name = "query",
      description = "The search string that is used to find specific categories",
      required = true,
      example = "Tech")
  public List<Category> searchCategories(@RestQuery String query) {

    return categoryService.searchMax5(query);
  }
}
