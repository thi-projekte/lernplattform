/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.entity.StreakContinuation;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import de.thi.mynd.progressTracking.entity.StreakType;
import de.thi.mynd.progressTracking.repository.StreakPreferenceRepository;
import de.thi.mynd.progressTracking.repository.StreakRepository;
import de.thi.mynd.progressTracking.request.StreakPreferenceRequest;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public final class StreakServiceImpl implements StreakService {

  @Inject SecurityIdentity identity;

  @Inject StreakRepository streakRepository;

  @Inject StreakPreferenceRepository streakPreferenceRepository;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public List<StreakDto> getLatestStreaksForCurrentUser() {
    String creatorId = identity.getPrincipal().getName();
    List<Streak> latestStreaks = streakRepository.findNotEndedByCreatorId(creatorId);

    endStreaksIfNotActiveAnymore(latestStreaks);

    return mappingRegistry.mapList(latestStreaks, StreakDto.class);
  }

  @Override
  @Transactional
  public StreakDto getLatestPreferredStreakForUser(String creatorId) {
    StreakPreference preference = getStreakPreferenceForUser(creatorId);
    Optional<Streak> streakOptional =
        streakRepository.findNotEndedByCreatorIdAndType(creatorId, preference.type);

    if (streakOptional.isEmpty()) {
      return null;
    }

    Streak streak = streakOptional.get();
    return mappingRegistry.map(streak, StreakDto.class);
  }

  @Override
  @Transactional
  public void continueOrStartStreaksForCurrentUser() {
    String creatorId = identity.getPrincipal().getName();
    List<Streak> latestStreaks = streakRepository.findNotEndedByCreatorId(creatorId);
    List<StreakType> typesToCreate =
        new ArrayList<>(List.of(StreakType.DAILY, StreakType.WEEKLY, StreakType.MONTHLY));

    StreakContinuation newContinuation = new StreakContinuation();
    newContinuation.creatorId = creatorId;
    for (Streak streak : latestStreaks) {
      if (isStreakActive(streak)) {
        typesToCreate.remove(streak.type);
      }
      if (isStreakActive(streak) && !isStreakSatisfied(streak)) {
        streak.lastContinuedAt = LocalDateTime.now();
        streak.continuations.add(newContinuation);
        streak.streakCount += 1;
        streakRepository.persist(streak);
        Log.infof("Continued streak %s for user %s", streak.id, streak.creatorId);
      }
    }

    for (StreakType type : typesToCreate) {
      Streak newStreak = new Streak();
      newStreak.type = type;
      newStreak.creatorId = creatorId;
      newStreak.startedAt = LocalDateTime.now();
      newStreak.lastContinuedAt = LocalDateTime.now();
      newStreak.streakCount = 1;
      newStreak.continuations.add(newContinuation);

      streakRepository.persist(newStreak);
      Log.infof("Started streak %s for user %s", newStreak.id, newStreak.creatorId);
    }

    streakRepository.flush();
  }

  @Override
  @Transactional
  public void endStreaksIfNotActiveAnymore(List<Streak> streaks) {
    for (Streak streak : streaks) {
      if (!isStreakActive(streak)) {
        streak.endedAt = streak.lastContinuedAt;
        streakRepository.persist(streak);
        Log.infof("The streak %s of the user %s ended", streak.id, streak.creatorId);
      }
    }
    streakRepository.flush();
  }

  @Override
  @Transactional
  public StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser() {
    String creatorId = identity.getPrincipal().getName();
    StreakPreference preference = getStreakPreferenceForUser(creatorId);

    return mappingRegistry.map(preference, StreakPreferenceDto.class);
  }

  @Override
  @Transactional
  public void updateStreakPreferencesForCurrentUser(StreakPreferenceRequest request) {
    String creatorId = identity.getPrincipal().getName();
    StreakPreference preference = getStreakPreferenceForUser(creatorId);

    preference.type = request.type;
    preference.isPublic = request.isPublic;

    streakPreferenceRepository.persistAndFlush(preference);

    Log.infof("Updated streak preferences for user %s", creatorId);
  }

  @Override
  public boolean isStreakActive(Streak streak) {
    if (streak.endedAt != null) {
      return false;
    }
    LocalDate today = LocalDate.now();
    LocalDate lastActivity = streak.lastContinuedAt.toLocalDate();

    switch (streak.type) {
      case DAILY -> {
        return lastActivity.equals(today) || lastActivity.equals(today.minusDays(1));
      }
      case WEEKLY -> {
        LocalDate startOfThisWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        return !lastActivity.isBefore(startOfLastWeek);
      }
      case MONTHLY -> {
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth lastMonth = thisMonth.minusMonths(1);
        YearMonth activityMonth = YearMonth.from(lastActivity);
        return activityMonth.equals(thisMonth) || activityMonth.equals(lastMonth);
      }
      default -> throw new IllegalArgumentException("Unknown streak type: " + streak.type);
    }
  }

  @Override
  public boolean isStreakSatisfied(Streak streak) {
    if (streak.endedAt != null) {
      return false;
    }
    LocalDate today = LocalDate.now();
    LocalDate lastActivity = streak.lastContinuedAt.toLocalDate();

    switch (streak.type) {
      case DAILY -> {
        return lastActivity.equals(today);
      }
      case WEEKLY -> {
        LocalDate startOfThisWeek = today.with(DayOfWeek.MONDAY);
        return !lastActivity.isBefore(startOfThisWeek);
      }
      case MONTHLY -> {
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth activityMonth = YearMonth.from(lastActivity);
        return activityMonth.equals(thisMonth);
      }
      default -> throw new IllegalArgumentException("Unknown streak type: " + streak.type);
    }
  }

  private StreakPreference getStreakPreferenceForUser(String creatorId) {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = creatorId;

    return streakPreferenceRepository
        .findByIdOptional(id)
        .orElseGet(() -> createStreakPreferenceForUser(creatorId));
  }

  private StreakPreference createStreakPreferenceForUser(String creatorId) {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = creatorId;
    StreakPreference preference = new StreakPreference();
    preference.id = id;
    preference.isPublic = false;
    preference.type = StreakType.DAILY;
    streakPreferenceRepository.persistAndFlush(preference);

    Log.infof("Created streak preferences for user %s", preference.creatorId);

    return preference;
  }
}
