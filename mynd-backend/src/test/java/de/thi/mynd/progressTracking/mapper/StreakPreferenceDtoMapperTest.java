/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import de.thi.mynd.progressTracking.entity.StreakType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StreakPreferenceDtoMapperTest {

  @Inject StreakPreferenceDtoMapper streakPreferenceDtoMapper;

  private StreakPreference streakPreference() {
    StreakPreference preference = new StreakPreference();
    preference.creatorId = "alice";
    preference.type = StreakType.WEEKLY;
    preference.isPublic = true;
    return preference;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    StreakPreference preference = streakPreference();

    StreakPreferenceDto dto = streakPreferenceDtoMapper.mapAndEnrich(preference);

    assertEquals(preference.creatorId, dto.creatorId);
    assertEquals(preference.type, dto.type);
    assertEquals(preference.isPublic, dto.isPublic);
  }

  @Test
  void mapAndEnrich_isPublicFalse_isCopiedAsFalse() {
    StreakPreference preference = streakPreference();
    preference.isPublic = false;

    StreakPreferenceDto dto = streakPreferenceDtoMapper.mapAndEnrich(preference);

    assertFalse(dto.isPublic);
  }

  @Test
  void getEntityType_returnsStreakPreference() {
    assertEquals(StreakPreference.class, streakPreferenceDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsStreakPreferenceDto() {
    assertEquals(StreakPreferenceDto.class, streakPreferenceDtoMapper.getDtoType());
  }
}
