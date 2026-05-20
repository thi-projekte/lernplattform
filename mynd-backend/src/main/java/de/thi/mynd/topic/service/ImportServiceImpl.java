package de.thi.mynd.topic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.dto.importer.FullImportDto;
import de.thi.mynd.topic.dto.importer.ImportCategoryDto;
import de.thi.mynd.topic.dto.importer.ImportTopicDto;
import de.thi.mynd.topic.entity.*;
import de.thi.mynd.topic.exception.ImportException;
import de.thi.mynd.topic.importer.ImportContext;
import de.thi.mynd.topic.repository.CategoryRepository;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.repository.TopicAssociationRepository;
import de.thi.mynd.topic.repository.TopicRepository;
import de.thi.mynd.topic.security.TopicVoter;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public final class ImportServiceImpl implements ImportService {

  @Inject TopicRepository topicRepository;
  @Inject CategoryRepository categoryRepository;
  @Inject TopicAssociationRepository topicAssociationRepository;
  @Inject ContentElementRepository contentElementRepository;
  @Inject SecurityIdentity identity;
  @Inject SecurityService securityService;
  @Inject ObjectMapper mapper;

  @Override
  @Transactional
  public void importFull(FullImportDto dto, boolean backendMode) {
    ImportContext ctx = new ImportContext(backendMode);
    ctx = doImportTopics(dto.getTopics(), ctx);
    doImportAssociations(dto.getAssociations(), ctx);
  }

  @Override
  @Transactional
  public ImportContext importCategories(List<ImportCategoryDto> dtos, ImportContext ctx) {
    return doImportCategories(dtos, ctx);
  }

  @Override
  @Transactional
  public ImportContext importTopics(List<ImportTopicDto> dtos, ImportContext ctx) {
    return doImportTopics(dtos, ctx);
  }

  @Override
  @Transactional
  public void importAssociations(Map<String, List<String>> associations, ImportContext ctx) {
    doImportAssociations(associations, ctx);
  }

  private ImportContext doImportCategories(List<ImportCategoryDto> dtos, ImportContext ctx) {
    int count = 0;
    Map<String, Category> mapping = new HashMap<>();
    String creatorId = resolveCreatorId(ctx);

    for (ImportCategoryDto model : dtos) {
      Category category = new Category();
      category.creatorId = creatorId;
      category.title = model.getTitle();
      category.color = model.getColor();

      // TODO: Add checks here later.

      categoryRepository.persist(category);

      mapping.put(model.getIdentifier(), category);
      count++;
    }

    categoryRepository.flush();

    Log.infof("The user %s successfully imported %d categories", creatorId, count);

    return ctx.withCategoryMapping(mapping);
  }

  private ImportContext doImportTopics(List<ImportTopicDto> dtos, ImportContext ctx) {
    int count = 0;
    Map<String, Topic> mapping = new HashMap<>();
    String creatorId = resolveCreatorId(ctx);

    for (ImportTopicDto dto : dtos) {
      Topic topic = new Topic();
      topic.title = dto.getTitle();
      topic.teaser = dto.getTeaser();
      topic.creatorId = creatorId;
      topic.estimatedLearningDuration = dto.getDuration();
      topic.categories =
          dto.getCategories().stream().map(key -> resolveCategory(key, ctx)).toList();

      if (!ctx.isBackendMode()) {
        securityService.denyUnlessGranted(topic, TopicVoter.Create);
      }

      topicRepository.persist(topic);
      mapping.put(dto.getIdentifier(), topic);

      dto.getContentElements().stream()
          .map(this::mapToContentElement)
          .forEach(
              ce -> {
                ce.topic = topic;
                contentElementRepository.persist(ce);
              });

      count++;
    }

    topicRepository.flush();

    Log.infof("The user %s successfully imported %d topics", creatorId, count);
    return ctx.withTopicMapping(mapping);
  }

  private void doImportAssociations(Map<String, List<String>> associations, ImportContext ctx) {
    int count = 0;
    String creatorId = resolveCreatorId(ctx);

    for (var entry : associations.entrySet()) {
      Topic owningTopic = resolveTopic(entry.getKey(), ctx);

      if (!ctx.isBackendMode()) {
        securityService.denyUnlessGranted(owningTopic, TopicVoter.AssignForeignTopics);
      }

      for (String foreignKey : entry.getValue()) {
        Topic foreignTopic = resolveTopic(foreignKey, ctx);

        if (topicAssociationRepository.associationExists(owningTopic, foreignTopic)) {
          continue;
        }

        TopicAssociation association = new TopicAssociation();
        association.creatorId = creatorId;
        association.owningTopic = owningTopic;
        association.foreignTopic = foreignTopic;
        topicAssociationRepository.persist(association);
        count++;
      }
    }

    topicAssociationRepository.flush();
    Log.infof("User %s created %d new associations", creatorId, count);
  }

  private String resolveCreatorId(ImportContext ctx) {
    return ctx.isBackendMode() ? "admin" : identity.getPrincipal().getName();
  }

  private Category resolveCategory(String key, ImportContext ctx) {
    if (ctx.getCategoryMapping().containsKey(key)) {
      return ctx.getCategoryMapping().get(key);
    }
    try {
      UUID id = UUID.fromString(key);
      return categoryRepository
          .findByIdOptional(id)
          .orElseThrow(() -> new ImportException("Category not found by ID: " + key));
    } catch (IllegalArgumentException e) {
      // If it's not a UUID, treat the key as a title instead
      return categoryRepository
          .findByTitleOptional(key)
          .orElseThrow(() -> new ImportException("Category not found by ID or title: " + key));
    }
  }

  private Topic resolveTopic(String key, ImportContext ctx) {
    if (ctx.getTopicMapping().containsKey(key)) {
      return ctx.getTopicMapping().get(key);
    }
    try {
      UUID id = UUID.fromString(key);
      return topicRepository
          .findByIdOptional(id)
          .orElseThrow(() -> new ImportException("Topic not found by ID: " + key));
    } catch (IllegalArgumentException e) {
      // If it's not a UUID, treat the key as a title instead
      return topicRepository
          .findByTitleOptional(key)
          .orElseThrow(() -> new ImportException("Topic not found by ID or title: " + key));
    }
  }

  private ContentElement mapToContentElement(Map<String, Object> generic) {
    return (ContentElement)
        mapper.convertValue(generic, resolveContentElementClass((String) generic.get("type")));
  }

  private Class<?> resolveContentElementClass(String type) {
    return switch (type) {
      case "RTF" -> RtfElement.class;
      case "SPOTIFY_LINK" -> SpotifyLinkElement.class;
      case "URI" -> UriElement.class;
      case "YOUTUBE_LINK" -> YouTubeLinkElement.class;
      default ->
          throw new ImportException("Only Rtf, Spotify Links, Uris and YouTube Links are suppored");
    };
  }
}
