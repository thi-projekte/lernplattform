package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.service.CategoryService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public final class CategoryResource {

  @Inject
  CategoryService categoryService;

  @Path("/search")
  @GET
  public List<Category> searchCategories(@RestQuery String query) {
    return categoryService.search(query);
  }
}
