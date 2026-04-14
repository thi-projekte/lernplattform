package de.thi.mynd.topic.service;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.repository.CategoryRepository;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.service.spi.InjectService;

@ApplicationScoped
public final class CategoryInitializerService {

    @Inject
    CategoryRepository categoryRepository;

    @Transactional
    public void initializeCategories(@Observes StartupEvent e) {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category technology = new Category();
        technology.title = "Technology";
        technology.color = "EE4B2B";
        categoryRepository.persist(technology);

        Category health = new Category();
        health.title = "Health";
        health.color = "32CD32";
        categoryRepository.persist(health);

        Category languages = new Category();
        languages.title = "Languages";
        languages.color = "00CEC8";
        categoryRepository.persist(languages);

        categoryRepository.flush();
    }
}
