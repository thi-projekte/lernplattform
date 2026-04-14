package de.thi.mynd.topic.service;

import de.thi.mynd.topic.entity.Category;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
public final class CategoryInitializerService {

    @Transactional
    public void initializeCategories(@Observes StartupEvent e) {
        if (Category.count() > 0) {
            return;
        }

        Category technology = new Category();
        technology.title = "Technology";
        technology.color = "EE4B2B";
        technology.persist();

        Category health = new Category();
        health.title = "Health";
        health.color = "32CD32";
        health.persist();

        Category languages = new Category();
        languages.title = "Languages";
        languages.color = "00CEC8";
        languages.persist();

        Panache.flush();
    }
}
