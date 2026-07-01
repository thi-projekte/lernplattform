/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import de.thi.mynd.progressTracking.entity.StreakType;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link StreakPreferenceRepository} against a real Postgres instance (Quarkus dev
 * services). This repository declares no custom query methods, so only the inherited behavior
 * (persistence with a {@link CreatorIdKey} primary key, lookup by id, and pagination) is tested.
 */
@QuarkusTest
class StreakPreferenceRepositoryTest {

  @Inject StreakPreferenceRepository streakPreferenceRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private StreakPreference newStreakPreference(String creatorId, StreakType type) {
    StreakPreference preference = new StreakPreference();
    CreatorIdKey key = new CreatorIdKey();
    key.creatorId = creatorId;
    preference.id = key;
    preference.creatorId = creatorId;
    preference.type = type;
    preference.isPublic = true;
    return preference;
  }

  @Test
  @TestTransaction
  void persistAndFindById_roundTripsEntity() {
    String creatorId = unique("creator");
    StreakPreference preference = newStreakPreference(creatorId, StreakType.DAILY);
    streakPreferenceRepository.persistAndFlush(preference);

    CreatorIdKey lookupKey = new CreatorIdKey();
    lookupKey.creatorId = creatorId;
    StreakPreference found = streakPreferenceRepository.findById(lookupKey);

    assertNotNull(found);
    assertEquals(StreakType.DAILY, found.type);
    assertTrue(found.isPublic);
  }

  @Test
  @TestTransaction
  void findById_missingId_returnsNull() {
    CreatorIdKey lookupKey = new CreatorIdKey();
    lookupKey.creatorId = unique("missing");

    StreakPreference found = streakPreferenceRepository.findById(lookupKey);

    assertNull(found);
  }

  @Test
  @TestTransaction
  void findAllWithLimit_respectsLimit() {
    for (int i = 0; i < 5; i++) {
      streakPreferenceRepository.persistAndFlush(
          newStreakPreference(unique("creator-" + i), StreakType.WEEKLY));
    }

    List<StreakPreference> result = streakPreferenceRepository.findAllWithLimit(3);

    assertEquals(3, result.size());
  }
}
