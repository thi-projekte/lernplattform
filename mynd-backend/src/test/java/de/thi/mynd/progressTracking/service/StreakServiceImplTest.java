package de.thi.mynd.progressTracking.service;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.dto.StreakPreferenceDto;
import de.thi.mynd.progressTracking.entity.Streak;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import de.thi.mynd.progressTracking.entity.StreakType;
import de.thi.mynd.progressTracking.repository.StreakPreferenceRepository;
import de.thi.mynd.progressTracking.repository.StreakRepository;
import de.thi.mynd.progressTracking.request.StreakPreferenceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class StreakServiceImplTest {

    @Inject
    StreakService streakService;

    @InjectMock
    SecurityIdentity identity;

    @InjectMock
    StreakRepository streakRepository;

    @InjectMock
    StreakPreferenceRepository streakPreferenceRepository;

    @InjectMock
    MappingRegistry mappingRegistry;

    private static final String CREATOR_ID = "test-user-123";

    @BeforeEach
    void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(CREATOR_ID);
        when(identity.getPrincipal()).thenReturn(principal);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Streak buildStreak(StreakType type, LocalDateTime lastContinuedAt) {
        Streak streak = new Streak();
        streak.type = type;
        streak.creatorId = CREATOR_ID;
        streak.lastContinuedAt = lastContinuedAt;
        streak.continuations = new ArrayList<>();
        return streak;
    }

    private Streak buildEndedStreak(StreakType type) {
        Streak streak = buildStreak(type, LocalDateTime.now().minusDays(10));
        streak.endedAt = LocalDateTime.now().minusDays(5);
        return streak;
    }

    // =========================================================================
    // isStreakActive
    // =========================================================================

    @Nested
    class IsStreakActive {

        @Test
        void returnsFalse_whenStreakIsAlreadyEnded() {
            Streak streak = buildEndedStreak(StreakType.DAILY);
            assertFalse(streakService.isStreakActive(streak));
        }

        // --- DAILY ---

        @Test
        void daily_returnsTrue_whenLastActivityIsToday() {
            Streak streak = buildStreak(StreakType.DAILY, LocalDateTime.now());
            assertTrue(streakService.isStreakActive(streak));
        }

        @Test
        void daily_returnsTrue_whenLastActivityIsYesterday() {
            Streak streak = buildStreak(StreakType.DAILY, LocalDateTime.now().minusDays(1));
            assertTrue(streakService.isStreakActive(streak));
        }

        @Test
        void daily_returnsFalse_whenLastActivityIsTwoDaysAgo() {
            Streak streak = buildStreak(StreakType.DAILY, LocalDateTime.now().minusDays(2));
            assertFalse(streakService.isStreakActive(streak));
        }

        // --- WEEKLY ---

        @Test
        void weekly_returnsTrue_whenLastActivityIsThisWeek() {
            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
            Streak streak = buildStreak(StreakType.WEEKLY, monday.atStartOfDay());
            assertTrue(streakService.isStreakActive(streak));
        }

        @Test
        void weekly_returnsTrue_whenLastActivityIsLastWeek() {
            LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
            Streak streak = buildStreak(StreakType.WEEKLY, lastMonday.atStartOfDay());
            assertTrue(streakService.isStreakActive(streak));
        }

        @Test
        void weekly_returnsFalse_whenLastActivityIsTwoWeeksAgo() {
            LocalDate twoWeeksAgoMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(2);
            Streak streak = buildStreak(StreakType.WEEKLY, twoWeeksAgoMonday.atStartOfDay());
            assertFalse(streakService.isStreakActive(streak));
        }

        // --- MONTHLY ---

        @Test
        void monthly_returnsTrue_whenLastActivityIsThisMonth() {
            Streak streak = buildStreak(StreakType.MONTHLY, LocalDateTime.now());
            assertTrue(streakService.isStreakActive(streak));
        }

        @Test
        void monthly_returnsTrue_whenLastActivityIsLastMonth() {
            Streak streak = buildStreak(StreakType.MONTHLY, LocalDateTime.now().minusMonths(1));
            assertTrue(streakService.isStreakActive(streak));
        }

        @Test
        void monthly_returnsFalse_whenLastActivityIsTwoMonthsAgo() {
            Streak streak = buildStreak(StreakType.MONTHLY, LocalDateTime.now().minusMonths(2));
            assertFalse(streakService.isStreakActive(streak));
        }
    }

    // =========================================================================
    // isStreakSatisfied
    // =========================================================================

    @Nested
    class IsStreakSatisfied {

        @Test
        void returnsFalse_whenStreakIsEnded() {
            Streak streak = buildEndedStreak(StreakType.DAILY);
            assertFalse(streakService.isStreakSatisfied(streak));
        }

        // --- DAILY ---

        @Test
        void daily_returnsTrue_whenLastActivityIsToday() {
            Streak streak = buildStreak(StreakType.DAILY, LocalDateTime.now());
            assertTrue(streakService.isStreakSatisfied(streak));
        }

        @Test
        void daily_returnsFalse_whenLastActivityIsYesterday() {
            // Active but not yet satisfied today
            Streak streak = buildStreak(StreakType.DAILY, LocalDateTime.now().minusDays(1));
            assertFalse(streakService.isStreakSatisfied(streak));
        }

        // --- WEEKLY ---

        @Test
        void weekly_returnsTrue_whenLastActivityIsThisWeek() {
            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
            Streak streak = buildStreak(StreakType.WEEKLY, monday.atStartOfDay());
            assertTrue(streakService.isStreakSatisfied(streak));
        }

        @Test
        void weekly_returnsFalse_whenLastActivityIsLastWeek() {
            LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
            Streak streak = buildStreak(StreakType.WEEKLY, lastMonday.atStartOfDay());
            assertFalse(streakService.isStreakSatisfied(streak));
        }

        // --- MONTHLY ---

        @Test
        void monthly_returnsTrue_whenLastActivityIsThisMonth() {
            Streak streak = buildStreak(StreakType.MONTHLY, LocalDateTime.now());
            assertTrue(streakService.isStreakSatisfied(streak));
        }

        @Test
        void monthly_returnsFalse_whenLastActivityIsLastMonth() {
            // Active but not yet satisfied this month
            Streak streak = buildStreak(StreakType.MONTHLY, LocalDateTime.now().minusMonths(1));
            assertFalse(streakService.isStreakSatisfied(streak));
        }
    }

    // =========================================================================
    // getLatestStreaksForCurrentUser
    // =========================================================================

    @Nested
    class GetLatestStreaks {

        @Test
        void returnsEmptyList_whenNoStreaksExist() {
            when(streakRepository.findNotEndedByCreatorId(CREATOR_ID)).thenReturn(List.of());
            when(mappingRegistry.mapList(anyList(), eq(StreakDto.class))).thenReturn(List.of());

            List<StreakDto> result = streakService.getLatestStreaksForCurrentUser();

            assertTrue(result.isEmpty());
        }

        @Test
        void endsExpiredStreaks_andReturnsMappedDtos() {
            Streak expiredStreak = buildStreak(StreakType.DAILY, LocalDateTime.now().minusDays(5));
            List<Streak> streaks = List.of(expiredStreak);

            when(streakRepository.findNotEndedByCreatorId(CREATOR_ID)).thenReturn(streaks);
            when(mappingRegistry.mapList(anyList(), eq(StreakDto.class))).thenReturn(List.of(StreakDto.builder().build()));

            List<StreakDto> result = streakService.getLatestStreaksForCurrentUser();

            assertEquals(1, result.size());
            // expired streak should be persisted with endedAt set
            verify(streakRepository).persist(expiredStreak);
            assertNotNull(expiredStreak.endedAt);
        }
    }

    // =========================================================================
    // continueOrStartStreaksForCurrentUser
    // =========================================================================

    @Nested
    class ContinueOrStartStreaks {

        @Test
        void createsAllThreeStreakTypes_whenNoExistingStreaks() {
            when(streakRepository.findNotEndedByCreatorId(CREATOR_ID)).thenReturn(List.of());

            streakService.continueOrStartStreaksForCurrentUser();

            // All 3 types must be created
            verify(streakRepository, times(3)).persist(any(Streak.class));
            verify(streakRepository).flush();
        }

        @Test
        void continuesActiveStreaks_andCreatesOnlyMissingTypes() {
            Streak activeDaily = buildStreak(StreakType.DAILY, LocalDateTime.now());

            when(streakRepository.findNotEndedByCreatorId(CREATOR_ID)).thenReturn(List.of(activeDaily));

            streakService.continueOrStartStreaksForCurrentUser();

            // 1 continued + 2 new (WEEKLY, MONTHLY)
            verify(streakRepository, times(3)).persist(any(Streak.class));
        }

        @Test
        void doesNotCreateNewStreak_whenAllTypesAreActive() {
            Streak daily   = buildStreak(StreakType.DAILY,   LocalDateTime.now());
            Streak weekly  = buildStreak(StreakType.WEEKLY,  LocalDateTime.now());
            Streak monthly = buildStreak(StreakType.MONTHLY, LocalDateTime.now());

            when(streakRepository.findNotEndedByCreatorId(CREATOR_ID))
                    .thenReturn(List.of(daily, weekly, monthly));

            streakService.continueOrStartStreaksForCurrentUser();

            // Only continuations — no new streak objects
            verify(streakRepository, times(3)).persist(any(Streak.class));
            // Each active streak gets a new continuation added
            assertEquals(1, daily.continuations.size());
            assertEquals(1, weekly.continuations.size());
            assertEquals(1, monthly.continuations.size());
        }

        @Test
        void inactiveStreakIsNotContinued_andNewOneIsCreated() {
            Streak inactiveDaily = buildStreak(StreakType.DAILY, LocalDateTime.now().minusDays(5));

            when(streakRepository.findNotEndedByCreatorId(CREATOR_ID))
                    .thenReturn(List.of(inactiveDaily));

            streakService.continueOrStartStreaksForCurrentUser();

            // inactive one is not continued — still 3 new ones created
            assertEquals(0, inactiveDaily.continuations.size());
            verify(streakRepository, times(3)).persist(any(Streak.class));
        }
    }

    // =========================================================================
    // endStreaksIfNotActiveAnymore
    // =========================================================================

    @Nested
    class EndStreaksIfNotActiveAnymore {

        @Test
        void setsEndedAt_forExpiredStreaks() {
            Streak expired = buildStreak(StreakType.DAILY, LocalDateTime.now().minusDays(3));

            streakService.endStreaksIfNotActiveAnymore(List.of(expired));

            assertNotNull(expired.endedAt);
            assertEquals(expired.lastContinuedAt, expired.endedAt);
            verify(streakRepository).persist(expired);
        }

        @Test
        void doesNotEndActiveStreak() {
            Streak active = buildStreak(StreakType.DAILY, LocalDateTime.now());

            streakService.endStreaksIfNotActiveAnymore(List.of(active));

            assertNull(active.endedAt);
            verify(streakRepository, never()).persist(active);
        }

        @Test
        void handlesEmptyList() {
            assertDoesNotThrow(() -> streakService.endStreaksIfNotActiveAnymore(List.of()));
            verify(streakRepository, never()).persist(any(Streak.class));
        }
    }

    // =========================================================================
    // StreakPreference
    // =========================================================================

    @Nested
    class StreakPreferences {

        @Test
        void getOrCreate_returnsExisting_whenFound() {
            StreakPreference existing = new StreakPreference();
            existing.creatorId = CREATOR_ID;

            when(streakPreferenceRepository.findByIdOptional(any(CreatorIdKey.class)))
                    .thenReturn(Optional.of(existing));
            when(mappingRegistry.map(existing, StreakPreferenceDto.class))
                    .thenReturn(StreakPreferenceDto.builder().build());

            StreakPreferenceDto result = streakService.getOrCreateStreakPreferenceForCurrentUser();

            assertNotNull(result);
            verify(streakPreferenceRepository, never()).persistAndFlush(any());
        }

        @Test
        void getOrCreate_createsNew_whenNotFound() {
            when(streakPreferenceRepository.findByIdOptional(any(CreatorIdKey.class)))
                    .thenReturn(Optional.empty());
            when(mappingRegistry.map(any(StreakPreference.class), eq(StreakPreferenceDto.class)))
                    .thenReturn(StreakPreferenceDto.builder().build());

            StreakPreferenceDto result = streakService.getOrCreateStreakPreferenceForCurrentUser();

            assertNotNull(result);
            verify(streakPreferenceRepository).persistAndFlush(any(StreakPreference.class));
        }

        @Test
        void update_persistsChangedPreference() {
            StreakPreference existing = new StreakPreference();
            existing.creatorId = CREATOR_ID;
            existing.type = StreakType.DAILY;
            existing.isPublic = false;

            when(streakPreferenceRepository.findByIdOptional(any(CreatorIdKey.class)))
                    .thenReturn(Optional.of(existing));

            StreakPreferenceRequest request = new StreakPreferenceRequest();
            request.type = StreakType.WEEKLY;
            request.isPublic = true;

            streakService.updateStreakPreferencesForCurrentUser(request);

            assertEquals(StreakType.WEEKLY, existing.type);
            assertTrue(existing.isPublic);
            verify(streakPreferenceRepository).persistAndFlush(existing);
        }
    }
}