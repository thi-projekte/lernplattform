package de.thi.mynd.demoContent.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.demoContent.event.LoadedCategoriesEvent;
import de.thi.mynd.topic.dto.importer.ImportCategoryDto;
import de.thi.mynd.topic.importer.ImportContext;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.service.ImportService;
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
import java.util.List;

@ApplicationScoped
@LookupIfProperty(name = "mynd.loadDemoContent", stringValue = "true")
public final class CategoryLoader {

  @Inject Event<LoadedCategoriesEvent> categoriesEventEvent;
  @Inject CategoryRepository categoryRepository;

  @Inject ImportService importService;

  @Inject ObjectMapper mapper;

  @Transactional
  public void initializeCategories(@Observes StartupEvent e) throws IOException {
    if (categoryRepository.count() > 0) {
      Log.info("Categories are already initialized");
      return;
    }

    ImportContext ctx = new ImportContext(true);

    List<ImportCategoryDto> models = loadJson();
    ctx = importService.importCategories(models, ctx);

    Log.info("Successfully initialized categories");
    categoriesEventEvent.fire(new LoadedCategoriesEvent(ctx));
  }

  private List<ImportCategoryDto> loadJson() throws IOException {
    try (InputStream is =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("demo-content/categories.json")) {
      return mapper.readValue(is, new TypeReference<List<ImportCategoryDto>>() {});
    }
  }
}
