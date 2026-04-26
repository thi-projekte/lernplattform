package de.thi.mynd.topic.service;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public final class CategoryInitializerService {

  @Inject CategoryRepository categoryRepository;
  @Inject TopicRepository topicRepository;

  @Transactional
  public void initializeCategories(@Observes StartupEvent e) {
    if (categoryRepository.count() > 0) {
      return;
    }

    Category technology = new Category();
    technology.creatorId = "admin";
    technology.title = "Technology";
    technology.color = "EE4B2B";
    categoryRepository.persist(technology);

    Topic technologyTopic = new Topic();
    technologyTopic.title = "Technology";
    technologyTopic.teaser = "Technology";
    technologyTopic.categories.add(technology);
    technologyTopic.estimatedLearningDuration = 2;
    technologyTopic.creatorId = "admin";
    topicRepository.persist(technologyTopic);

    Category health = new Category();
    health.creatorId = "admin";
    health.title = "Health";
    health.color = "32CD32";
    categoryRepository.persist(health);

    Topic healthTopic = new Topic();
    healthTopic.title = "Health";
    healthTopic.teaser = "Health";
    healthTopic.categories.add(health);
    healthTopic.estimatedLearningDuration = 2;
    healthTopic.creatorId = "admin";
    topicRepository.persist(healthTopic);

    Category languages = new Category();
    languages.creatorId = "admin";
    languages.title = "Languages";
    languages.color = "00CEC8";
    categoryRepository.persist(languages);

    Topic languagesTopic = new Topic();
    languagesTopic.title = "Languages";
    languagesTopic.teaser = "Languages";
    languagesTopic.categories.add(languages);
    languagesTopic.estimatedLearningDuration = 2;
    languagesTopic.creatorId = "admin";
    topicRepository.persist(languagesTopic);

    topicRepository.flush();
  }
}
