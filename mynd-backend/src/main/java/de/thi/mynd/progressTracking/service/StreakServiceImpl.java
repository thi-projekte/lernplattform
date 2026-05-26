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
  public void continueOrStartStreaksForCurrentUser() {
    String creatorId = identity.getPrincipal().getName();
    List<Streak> latestStreaks = streakRepository.findNotEndedByCreatorId(creatorId);
    List<StreakType> typesToCreate =
        new ArrayList<>(List.of(StreakType.DAILY, StreakType.WEEKLY, StreakType.MONTHLY));

    StreakContinuation newContinuation = new StreakContinuation();
    newContinuation.creatorId = creatorId;
    for (Streak streak : latestStreaks) {
      if (isStreakActive(streak) && !isStreakSatisfied(streak)) {
        streak.lastContinuedAt = LocalDateTime.now();
        streak.continuations.add(newContinuation);
        streakRepository.persist(streak);
        typesToCreate.remove(streak.type);
      }
    }

    for (StreakType type : typesToCreate) {
      Streak newStreak = new Streak();
      newStreak.type = type;
      newStreak.creatorId = creatorId;
      newStreak.startedAt = LocalDateTime.now();
      newStreak.lastContinuedAt = LocalDateTime.now();
      newStreak.continuations.add(newContinuation);

      streakRepository.persist(newStreak);
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
      }
    }
    streakRepository.flush();
  }

  @Override
  @Transactional
  public StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser() {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = identity.getPrincipal().getName();
    StreakPreference preference =
        streakPreferenceRepository
            .findByIdOptional(id)
            .orElseGet(this::createStreakPreferenceForCurrentUser);

    return mappingRegistry.map(preference, StreakPreferenceDto.class);
  }

  @Override
  @Transactional
  public void updateStreakPreferencesForCurrentUser(StreakPreferenceRequest request) {
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = identity.getPrincipal().getName();
    StreakPreference preference =
        streakPreferenceRepository
            .findByIdOptional(id)
            .orElseGet(this::createStreakPreferenceForCurrentUser);

    preference.type = request.type;
    preference.isPublic = request.isPublic;

    streakPreferenceRepository.persistAndFlush(preference);
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

  private StreakPreference createStreakPreferenceForCurrentUser() {
    StreakPreference preference = new StreakPreference();
    preference.creatorId = identity.getPrincipal().getName();
    preference.isPublic = false;
    preference.type = StreakType.DAILY;
    streakPreferenceRepository.persistAndFlush(preference);

    return preference;
  }
}
