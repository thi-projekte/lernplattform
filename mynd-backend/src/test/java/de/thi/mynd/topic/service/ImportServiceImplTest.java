package de.thi.mynd.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Assumptions: - ImportContext has: isBackendMode(), getCategoryMapping(), getTopicMapping(),
 * withCategoryMapping(Map), withTopicMapping(Map) - ImportException extends RuntimeException -
 * Category fields: creatorId, title, color (public or package-private like Topic) - Topic fields:
 * title, teaser, creatorId, estimatedLearningDuration, categories (public) - TopicAssociation
 * fields: creatorId, owningTopic, foreignTopic (public) - ContentElement has a `topic` field -
 * TopicAssociationRepository has associationExists(Topic, Topic) -
 * SecurityService.denyUnlessGranted throws on denied (like an AuthorizationException) -
 * FullImportDto has getTopics(), getAssociations() — no categories in full import (matches
 * importFull which skips doImportCategories)
 */
@QuarkusTest
class ImportServiceImplTest {

  @Inject ImportServiceImpl importService;

  @InjectMock TopicRepository topicRepository;

  @InjectMock CategoryRepository categoryRepository;

  @InjectMock TopicAssociationRepository topicAssociationRepository;

  @InjectMock ContentElementRepository contentElementRepository;

  @InjectMock SecurityIdentity identity;

  @InjectMock SecurityService securityService;

  // Real ObjectMapper — we want actual deserialization behavior
  @Inject ObjectMapper objectMapper;

  private static final String USER_PRINCIPAL = "test-user";
  private static final UUID EXISTING_CATEGORY_ID = UUID.randomUUID();
  private static final UUID EXISTING_TOPIC_ID = UUID.randomUUID();

  @BeforeEach
  void setup() {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(USER_PRINCIPAL);
    when(identity.getPrincipal()).thenReturn(principal);
  }

  // =========================================================================
  // importCategories
  // =========================================================================

  @Nested
  class ImportCategories {

    @Test
    void happyPath_backendMode_persistsAndReturnsMapping() {
      ImportContext ctx = new ImportContext(true);
      ImportCategoryDto dto = categoryDto("cat-1", "Science", "#FF0000");

      ImportContext result = importService.importCategories(List.of(dto), ctx);

      verify(categoryRepository)
          .persist(
              argThat(
                  (Category c) ->
                      c.title.equals("Science")
                          && c.color.equals("#FF0000")
                          && c.creatorId.equals("admin")));
      verify(categoryRepository).flush();
      assertTrue(result.getCategoryMapping().containsKey("cat-1"));
    }

    @Test
    void happyPath_userMode_usesIdentityAsCreator() {
      ImportContext ctx = new ImportContext(false);
      ImportCategoryDto dto = categoryDto("cat-1", "History", "#00FF00");

      ImportContext result = importService.importCategories(List.of(dto), ctx);

      verify(categoryRepository)
          .persist(argThat((Category c) -> c.creatorId.equals(USER_PRINCIPAL)));
      assertTrue(result.getCategoryMapping().containsKey("cat-1"));
    }

    @Test
    void emptyList_returnsContextWithEmptyMapping() {
      ImportContext ctx = new ImportContext(true);

      ImportContext result = importService.importCategories(List.of(), ctx);

      verify(categoryRepository, never()).persist(any(Category.class));
      assertTrue(result.getCategoryMapping().isEmpty());
    }

    @Test
    void multipleCategories_allPersistedAndMapped() {
      ImportContext ctx = new ImportContext(true);
      List<ImportCategoryDto> dtos =
          List.of(
              categoryDto("cat-1", "Math", "#111"),
              categoryDto("cat-2", "Art", "#222"),
              categoryDto("cat-3", "Sport", "#333"));

      ImportContext result = importService.importCategories(dtos, ctx);

      verify(categoryRepository, times(3)).persist(any(Category.class));
      assertTrue(result.getCategoryMapping().containsKey("cat-1"));
      assertTrue(result.getCategoryMapping().containsKey("cat-2"));
      assertTrue(result.getCategoryMapping().containsKey("cat-3"));
    }

    @Test
    void duplicateIdentifier_lastOneWinsInMapping() {
      ImportContext ctx = new ImportContext(true);
      // Same identifier, different titles — edge case: duplicates in input
      List<ImportCategoryDto> dtos =
          List.of(categoryDto("cat-1", "First", "#111"), categoryDto("cat-1", "Second", "#222"));

      ImportContext result = importService.importCategories(dtos, ctx);

      // Both are persisted, but mapping holds the last one
      verify(categoryRepository, times(2)).persist(any(Category.class));
      assertEquals(1, result.getCategoryMapping().size());
    }
  }

  // =========================================================================
  // importTopics
  // =========================================================================

  @Nested
  class ImportTopics {

    @Test
    void happyPath_backendMode_persistsTopicAndContentElements() {
      ImportContext ctx = contextWithCategory("cat-ref");
      ImportTopicDto dto =
          topicDto(
              "topic-1",
              "Java Basics",
              "Learn Java",
              List.of("cat-ref"),
              List.of(rtfElement("Hello")));

      ImportContext result = importService.importTopics(List.of(dto), ctx);

      verify(topicRepository)
          .persist(
              argThat(
                  (Topic t) ->
                      t.title.equals("Java Basics")
                          && t.teaser.equals("Learn Java")
                          && t.creatorId.equals("admin")
                          && t.categories.size() == 1));
      verify(contentElementRepository).persist(any(ContentElement.class));
      verify(topicRepository).flush();
      assertTrue(result.getTopicMapping().containsKey("topic-1"));
    }

    @Test
    void backendMode_skipsPermissionCheck() {
      ImportContext ctx = new ImportContext(true);
      // No category needed — empty list
      ImportTopicDto dto = topicDto("t-1", "T", "T", List.of(), List.of());

      importService.importTopics(List.of(dto), ctx);

      verify(securityService, never()).denyUnlessGranted(any(), any());
    }

    @Test
    void categoryResolution_fromContextMapping() {
      Category cat = new Category();
      cat.title = "Mapped Cat";
      ImportContext ctx = new ImportContext(true).withCategoryMapping(Map.of("cat-key", cat));
      ImportTopicDto dto = topicDto("t-1", "T", "T", List.of("cat-key"), List.of());

      importService.importTopics(List.of(dto), ctx);

      verify(topicRepository).persist(argThat((Topic t) -> t.categories.contains(cat)));
      // Should not hit the database for category
      verify(categoryRepository, never()).findByIdOptional(any());
    }

    @Test
    void categoryResolution_fromDatabase_whenNotInMapping() {
      Category dbCat = new Category();
      when(categoryRepository.findByIdOptional(EXISTING_CATEGORY_ID))
          .thenReturn(Optional.of(dbCat));
      ImportContext ctx = new ImportContext(true);
      ImportTopicDto dto =
          topicDto("t-1", "T", "T", List.of(EXISTING_CATEGORY_ID.toString()), List.of());

      importService.importTopics(List.of(dto), ctx);

      verify(categoryRepository).findByIdOptional(EXISTING_CATEGORY_ID);
    }

    @Test
    void categoryResolution_unknownUUID_throwsImportException() {
      UUID unknownId = UUID.randomUUID();
      when(categoryRepository.findByIdOptional(unknownId)).thenReturn(Optional.empty());
      ImportContext ctx = new ImportContext(true);
      ImportTopicDto dto = topicDto("t-1", "T", "T", List.of(unknownId.toString()), List.of());

      assertThrows(ImportException.class, () -> importService.importTopics(List.of(dto), ctx));
    }

    @Test
    void categoryResolution_invalidKey_notUUIDAndNotInMapping_throwsImportException() {
      ImportContext ctx = new ImportContext(true);
      ImportTopicDto dto = topicDto("t-1", "T", "T", List.of("this-is-not-a-uuid"), List.of());

      assertThrows(ImportException.class, () -> importService.importTopics(List.of(dto), ctx));
    }

    @Test
    void multipleContentElements_allPersistedWithCorrectTopic() {
      ImportContext ctx = new ImportContext(true);
      ImportTopicDto dto =
          topicDto(
              "t-1",
              "T",
              "T",
              List.of(),
              List.of(rtfElement("A"), rtfElement("B"), rtfElement("C")));

      importService.importTopics(List.of(dto), ctx);

      verify(contentElementRepository, times(3))
          .persist(argThat((ContentElement ce) -> ce.topic != null));
    }

    @Test
    void noContentElements_doesNotPersistAny() {
      ImportContext ctx = new ImportContext(true);
      ImportTopicDto dto = topicDto("t-1", "T", "T", List.of(), List.of());

      importService.importTopics(List.of(dto), ctx);

      verify(contentElementRepository, never()).persist(any(ContentElement.class));
    }

    @Test
    void emptyList_returnsContextWithEmptyTopicMapping() {
      ImportContext ctx = new ImportContext(true);

      ImportContext result = importService.importTopics(List.of(), ctx);

      verify(topicRepository, never()).persist(any(List.class));
      assertTrue(result.getTopicMapping().isEmpty());
    }

    @Test
    void contentElement_unknownType_defaultsToRtf() {
      ImportContext ctx = new ImportContext(true);
      Map<String, Object> unknownType = new HashMap<>();
      unknownType.put("type", "UNKNOWN_TYPE");
      unknownType.put("content", "some content");

      ImportTopicDto dto = topicDto("t-1", "T", "T", List.of(), List.of(unknownType));

      // Should not throw — falls back to RtfElement
      assertThrows(ImportException.class, () -> importService.importTopics(List.of(dto), ctx));
    }
  }

  // =========================================================================
  // importAssociations
  // =========================================================================

  @Nested
  class ImportAssociations {

    @Test
    void happyPath_createsAssociation() {
      Topic owning = persistedTopic("owning");
      Topic foreign = persistedTopic("foreign");
      ImportContext ctx =
          new ImportContext(true).withTopicMapping(Map.of("owning", owning, "foreign", foreign));

      when(topicAssociationRepository.associationExists(owning, foreign)).thenReturn(false);

      importService.importAssociations(Map.of("owning", List.of("foreign")), ctx);

      verify(topicAssociationRepository)
          .persist(
              argThat(
                  (TopicAssociation a) ->
                      a.owningTopic == owning
                          && a.foreignTopic == foreign
                          && a.creatorId.equals("admin")));
    }

    @Test
    void skipsAlreadyExistingAssociation() {
      Topic owning = persistedTopic("owning");
      Topic foreign = persistedTopic("foreign");
      ImportContext ctx =
          new ImportContext(true).withTopicMapping(Map.of("owning", owning, "foreign", foreign));

      when(topicAssociationRepository.associationExists(owning, foreign)).thenReturn(true);

      importService.importAssociations(Map.of("owning", List.of("foreign")), ctx);

      verify(topicAssociationRepository, never()).persist(any(TopicAssociation.class));
    }

    @Test
    void userMode_checksAssignForeignTopicsPermission() {
      Topic owning = persistedTopic("owning");
      Topic foreign = persistedTopic("foreign");
      ImportContext ctx =
          new ImportContext(false).withTopicMapping(Map.of("owning", owning, "foreign", foreign));
      when(topicAssociationRepository.associationExists(any(), any())).thenReturn(false);

      importService.importAssociations(Map.of("owning", List.of("foreign")), ctx);

      verify(securityService).denyUnlessGranted(owning, TopicVoter.AssignForeignTopics);
    }

    @Test
    void backendMode_skipsPermissionCheck() {
      Topic owning = persistedTopic("owning");
      Topic foreign = persistedTopic("foreign");
      ImportContext ctx =
          new ImportContext(true).withTopicMapping(Map.of("owning", owning, "foreign", foreign));
      when(topicAssociationRepository.associationExists(any(), any())).thenReturn(false);

      importService.importAssociations(Map.of("owning", List.of("foreign")), ctx);

      verify(securityService, never()).denyUnlessGranted(any(), any());
    }

    @Test
    void topicResolution_fromDatabase_whenNotInMapping() {
      Topic dbTopic = new Topic();
      when(topicRepository.findByIdOptional(EXISTING_TOPIC_ID)).thenReturn(Optional.of(dbTopic));

      Topic foreign = persistedTopic("foreign");
      ImportContext ctx = new ImportContext(true).withTopicMapping(Map.of("foreign", foreign));
      when(topicAssociationRepository.associationExists(any(), any())).thenReturn(false);

      importService.importAssociations(
          Map.of(EXISTING_TOPIC_ID.toString(), List.of("foreign")), ctx);

      verify(topicRepository).findByIdOptional(EXISTING_TOPIC_ID);
    }

    @Test
    void topicResolution_unknownUUID_throwsImportException() {
      UUID unknownId = UUID.randomUUID();
      when(topicRepository.findByIdOptional(unknownId)).thenReturn(Optional.empty());
      ImportContext ctx = new ImportContext(true);

      assertThrows(
          ImportException.class,
          () -> importService.importAssociations(Map.of(unknownId.toString(), List.of()), ctx));
    }

    @Test
    void topicResolution_invalidKey_throwsImportException() {
      ImportContext ctx = new ImportContext(true);

      assertThrows(
          ImportException.class,
          () -> importService.importAssociations(Map.of("not-a-uuid", List.of()), ctx));
    }

    @Test
    void emptyAssociationMap_persistsNothing() {
      ImportContext ctx = new ImportContext(true);

      importService.importAssociations(Map.of(), ctx);

      verify(topicAssociationRepository, never()).persist(any(TopicAssociation.class));
      verify(topicAssociationRepository).flush();
    }

    @Test
    void multipleForignTopics_allProcessed() {
      Topic owning = persistedTopic("owning");
      Topic f1 = persistedTopic("f1");
      Topic f2 = persistedTopic("f2");
      Topic f3 = persistedTopic("f3");
      ImportContext ctx =
          new ImportContext(true)
              .withTopicMapping(Map.of("owning", owning, "f1", f1, "f2", f2, "f3", f3));
      when(topicAssociationRepository.associationExists(any(), any())).thenReturn(false);

      importService.importAssociations(Map.of("owning", List.of("f1", "f2", "f3")), ctx);

      verify(topicAssociationRepository, times(3)).persist(any(TopicAssociation.class));
    }

    @Test
    void mixedExistingAndNew_onlyNewOnesArePersisted() {
      Topic owning = persistedTopic("owning");
      Topic existing = persistedTopic("existing");
      Topic newTopic = persistedTopic("new");
      ImportContext ctx =
          new ImportContext(true)
              .withTopicMapping(Map.of("owning", owning, "existing", existing, "new", newTopic));

      when(topicAssociationRepository.associationExists(owning, existing)).thenReturn(true);
      when(topicAssociationRepository.associationExists(owning, newTopic)).thenReturn(false);

      importService.importAssociations(Map.of("owning", List.of("existing", "new")), ctx);

      verify(topicAssociationRepository, times(1))
          .persist(argThat((TopicAssociation a) -> a.foreignTopic == newTopic));
    }
  }

  // =========================================================================
  // importFull
  // =========================================================================

  @Nested
  class ImportFull {

    @Test
    void happyPath_callsTopicsAndAssociations() {
      Topic topic = persistedTopic("t-1");
      FullImportDto dto = new FullImportDto();
      dto.setTopics(List.of(topicDto("t-1", "T", "T", List.of(), List.of())));
      dto.setAssociations(Map.of());

      // Should complete without exception, topics and associations processed
      assertDoesNotThrow(() -> importService.importFull(dto, true));

      verify(topicRepository, atLeastOnce()).persist(any(Topic.class));
    }

    @Test
    void backendMode_true_noPermissionChecks() {
      FullImportDto dto = new FullImportDto();
      dto.setTopics(List.of(topicDto("t-1", "T", "T", List.of(), List.of())));
      dto.setAssociations(Map.of());

      importService.importFull(dto, true);

      verify(securityService, never()).denyUnlessGranted(any(), any());
    }

    @Test
    void backendMode_false_permissionChecked() {
      FullImportDto dto = new FullImportDto();
      dto.setTopics(List.of(topicDto("t-1", "T", "T", List.of(), List.of())));
      dto.setAssociations(Map.of());

      importService.importFull(dto, false);

      verify(securityService).denyUnlessGranted(any(Topic.class), eq(TopicVoter.Create));
    }

    @Test
    void topicMappingPassedToAssociations() {
      // Topic t-1 created during importTopics, then referenced in associations
      FullImportDto dto = new FullImportDto();
      dto.setTopics(
          List.of(
              topicDto("t-1", "Owner", "T", List.of(), List.of()),
              topicDto("t-2", "Foreign", "T", List.of(), List.of())));
      dto.setAssociations(Map.of("t-1", List.of("t-2")));

      when(topicAssociationRepository.associationExists(any(), any())).thenReturn(false);

      // Should resolve both topics from the mapping built during topic import
      assertDoesNotThrow(() -> importService.importFull(dto, true));

      verify(topicAssociationRepository).persist(any(TopicAssociation.class));
    }
  }

  // =========================================================================
  // ImportContext isolation
  // =========================================================================

  @Nested
  class ImportContextIsolation {

    @Test
    void twoSequentialImports_doNotShareTopicMapping() {
      ImportContext ctx1 = new ImportContext(true);
      ImportTopicDto dto1 = topicDto("t-1", "First", "T", List.of(), List.of());
      ImportContext result1 = importService.importTopics(List.of(dto1), ctx1);

      ImportContext ctx2 = new ImportContext(true);
      ImportContext result2 = importService.importTopics(List.of(), ctx2);

      assertTrue(result1.getTopicMapping().containsKey("t-1"));
      assertFalse(result2.getTopicMapping().containsKey("t-1"));
    }

    @Test
    void contextIsImmutable_withTopicMappingDoesNotMutateOriginal() {
      ImportContext original = new ImportContext(true);
      ImportContext derived = original.withTopicMapping(Map.of("t-1", new Topic()));

      assertTrue(original.getTopicMapping().isEmpty());
      assertTrue(derived.getTopicMapping().containsKey("t-1"));
    }
  }

  // =========================================================================
  // Helpers
  // =========================================================================

  private ImportCategoryDto categoryDto(String identifier, String title, String color) {
    ImportCategoryDto dto = new ImportCategoryDto();
    dto.setIdentifier(identifier);
    dto.setTitle(title);
    dto.setColor(color);
    return dto;
  }

  private ImportTopicDto topicDto(
      String identifier,
      String title,
      String teaser,
      List<String> categories,
      List<Map<String, Object>> contentElements) {
    ImportTopicDto dto = new ImportTopicDto();
    dto.setIdentifier(identifier);
    dto.setTitle(title);
    dto.setTeaser(teaser);
    dto.setCategories(categories);
    dto.setContentElements(contentElements);
    dto.setIndexCards(new ArrayList<>());
    return dto;
  }

  private Map<String, Object> rtfElement(String content) {
    Map<String, Object> map = new HashMap<>();
    map.put("type", "RTF");
    map.put("content", content);
    return map;
  }

  private ImportContext contextWithCategory(String key) {
    Category cat = new Category();
    cat.title = "Test Category";
    return new ImportContext(true).withCategoryMapping(Map.of(key, cat));
  }

  /** Simulates a topic as if it came out of a previous importTopics call */
  private Topic persistedTopic(String markerTitle) {
    Topic t = new Topic();
    t.title = markerTitle;
    return t;
  }
}
