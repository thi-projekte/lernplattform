package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
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

  @Inject CategoryRepository categoryRepository;

  @Path("/search")
  @GET
  public List<Category> searchCategories(@RestQuery String query) {
    if (query == null) {
      return categoryRepository.findAllWithLimit(5);
    }
    return categoryRepository.findByTitleWithLimit(query, 5);
  }
}
