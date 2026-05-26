package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.CategoryDto;
import de.thi.mynd.topic.dto.CategoryTreeDto;
import de.thi.mynd.topic.request.CategoryRequest;
import de.thi.mynd.topic.service.CategoryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("authorizedUser")
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
  public List<CategoryDto> searchCategories(@RestQuery String query) {

    return categoryService.searchMax5(query);
  }

  @GET
  @Path("/tree")
  @RolesAllowed("admin")
  @Operation(
      summary = "Gets the whole tree",
      description = "Loads the whole category tree from the database")
  public List<CategoryTreeDto> getFullTree() {
    return categoryService.getFullTree();
  }

  @POST
  @RolesAllowed("admin")
  @Operation(summary = "Creates a new category", description = "Creates a completely new category")
  public Response createCategory(@Valid CategoryRequest request) {
    categoryService.createCategory(request);
    return Response.ok().build();
  }

  @PUT
  @Path("/{categoryId}")
  @RolesAllowed("admin")
  @Operation(
      summary = "Updates an existing category",
      description = "Sets all values of an existing category")
  public Response updateCategory(UUID categoryId, @Valid CategoryRequest request) {
    categoryService.updateCategory(categoryId, request);
    return Response.ok().build();
  }
}
