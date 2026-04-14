package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryRepository categoryRepository;


    @Path("/search")
    @GET
    public List<Category> searchCategories(@RestQuery String query) {
        if (query == null) {
            return categoryRepository.findAllWithLimit(5);
        }
        return categoryRepository.findByTitleWithLimit(query, 5);
    }
}
