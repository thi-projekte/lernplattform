package de.thi.mynd.demoContent.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.demoContent.event.LoadedCategoriesEvent;
import de.thi.mynd.demoContent.models.CategoryModel;
import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@LookupIfProperty(name = "mynd.loadDemoContent", stringValue = "true")
public final class CategoryLoader {

  @Inject
  Event<LoadedCategoriesEvent> categoriesEventEvent;
  @Inject CategoryRepository categoryRepository;

  @Inject
  ObjectMapper mapper;

  @Transactional
  public void initializeCategories(@Observes StartupEvent e) throws IOException {
    if (categoryRepository.count() > 0) {
      Log.info("Categories are already initialized");
      return;
    }

    List<CategoryModel> models = loadJson();

    Map<String, Category> mapping = new HashMap<>();

    for (CategoryModel model : models) {
      Category category = new Category();
      category.creatorId = model.getCreatorId();
      category.title = model.getTitle();
      category.color = model.getColor();
      categoryRepository.persist(category);

      mapping.put(model.getIdentifier(), category);

    }

    categoryRepository.flush();

    Log.info("Successfully initialized categories");
    categoriesEventEvent.fire(new LoadedCategoriesEvent(mapping));
  }

  private List<CategoryModel> loadJson() throws IOException {
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("demo-content/categories.json")) {
      return mapper.readValue(is, new TypeReference<List<CategoryModel>>() {});
    }
  }
}
