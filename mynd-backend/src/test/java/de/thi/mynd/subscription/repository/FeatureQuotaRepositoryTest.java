/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.subscription.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.FeatureQuotaId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link FeatureQuotaRepository} against a real Postgres instance (Quarkus dev services).
 * Every fixture creatorId is randomized to avoid primary key collisions and false positives from
 * pre-existing rows.
 */
@QuarkusTest
class FeatureQuotaRepositoryTest {

  @Inject FeatureQuotaRepository featureQuotaRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private FeatureQuota newFeatureQuota(String creatorId, Feature feature) {
    FeatureQuota featureQuota = new FeatureQuota();
    FeatureQuotaId id = new FeatureQuotaId();
    id.creatorId = creatorId;
    id.feature = feature;
    featureQuota.id = id;
    featureQuota.creatorId = creatorId;
    featureQuota.count = 0;
    return featureQuota;
  }

  @Test
  @TestTransaction
  void findByCreatorAndFeatureAndDate_existingMatch_returnsQuota() {
    String creatorId = unique("creator");
    LocalDate date = LocalDate.now();
    FeatureQuota featureQuota = newFeatureQuota(creatorId, Feature.StartTopic);
    featureQuota.dayAccountedFor = date;
    featureQuota.count = 4;
    featureQuotaRepository.persistAndFlush(featureQuota);

    Optional<FeatureQuota> result =
        featureQuotaRepository.findByCreatorAndFeatureAndDate(creatorId, Feature.StartTopic, date);

    assertTrue(result.isPresent());
    assertEquals(4, result.get().count);
  }

  @Test
  @TestTransaction
  void findByCreatorAndFeatureAndDate_dateMismatch_returnsEmpty() {
    String creatorId = unique("creator");
    FeatureQuota featureQuota = newFeatureQuota(creatorId, Feature.StartTopic);
    featureQuota.dayAccountedFor = LocalDate.now();
    featureQuotaRepository.persistAndFlush(featureQuota);

    Optional<FeatureQuota> result =
        featureQuotaRepository.findByCreatorAndFeatureAndDate(
            creatorId, Feature.StartTopic, LocalDate.now().minusDays(1));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByCreatorAndFeatureAndDate_noMatch_returnsEmpty() {
    Optional<FeatureQuota> result =
        featureQuotaRepository.findByCreatorAndFeatureAndDate(
            unique("missing"), Feature.LearnContentElementOrTopic, LocalDate.now());

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByCreatorAndFeature_existingMatch_returnsQuotaRegardlessOfDate() {
    String creatorId = unique("creator");
    FeatureQuota featureQuota = newFeatureQuota(creatorId, Feature.LearnContentElementOrTopic);
    featureQuota.dayAccountedFor = LocalDate.now().minusDays(10);
    featureQuota.count = 7;
    featureQuotaRepository.persistAndFlush(featureQuota);

    Optional<FeatureQuota> result =
        featureQuotaRepository.findByCreatorAndFeature(
            creatorId, Feature.LearnContentElementOrTopic);

    assertTrue(result.isPresent());
    assertEquals(7, result.get().count);
  }

  @Test
  @TestTransaction
  void findByCreatorAndFeature_noDayAccountedFor_stillMatches() {
    String creatorId = unique("creator");
    FeatureQuota featureQuota = newFeatureQuota(creatorId, Feature.StartTopic);
    featureQuota.dayAccountedFor = null;
    featureQuotaRepository.persistAndFlush(featureQuota);

    Optional<FeatureQuota> result =
        featureQuotaRepository.findByCreatorAndFeature(creatorId, Feature.StartTopic);

    assertTrue(result.isPresent());
  }

  @Test
  @TestTransaction
  void findByCreatorAndFeature_noMatch_returnsEmpty() {
    Optional<FeatureQuota> result =
        featureQuotaRepository.findByCreatorAndFeature(unique("missing"), Feature.StartTopic);

    assertTrue(result.isEmpty());
  }
}
