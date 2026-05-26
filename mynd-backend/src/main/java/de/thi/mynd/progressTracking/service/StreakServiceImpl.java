package de.thi.mynd.progressTracking.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.repository.StreakRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public final class StreakServiceImpl implements StreakService {

    @Inject
    SecurityIdentity identity;

    @Inject
    StreakRepository streakRepository;

    @Inject
    MappingRegistry mappingRegistry;

    @Override
    public List<StreakDto> getLatestStreaksForCurrentUser() {
        String creatorId = identity.getPrincipal().getName();
        List<Streak> latestStreaks = streakRepository.findNotEndedByCreatorId(creatorId);

        return mappingRegistry.mapList(latestStreaks, StreakDto.class);
    }

    @Override
    public void continueOrStartStreaksForCurrentUser() {

    }

    @Override
    public StreakPreferenceDto getOrCreateStreakPreferenceForCurrentUser() {
        return null;
    }

    @Override
    public void updateStreakPreferencesForCurrentUser() {

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
                return lastActivity.equals(today) ||
                        lastActivity.equals(today.minusDays(1));
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
                return activityMonth.equals(thisMonth) ||
                        activityMonth.equals(lastMonth);
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
                LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
                return !lastActivity.isBefore(startOfLastWeek);
            }
            case MONTHLY -> {
                YearMonth thisMonth = YearMonth.from(today);
                YearMonth activityMonth = YearMonth.from(lastActivity);
                return activityMonth.equals(thisMonth);
            }
            default -> throw new IllegalArgumentException("Unknown streak type: " + streak.type);
        }
    }
}
