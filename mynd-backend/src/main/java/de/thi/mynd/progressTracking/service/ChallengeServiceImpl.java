package de.thi.mynd.progressTracking.service;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.repository.UserProfileRepository;
import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.progressTracking.dto.ChallengeDto;
import de.thi.mynd.progressTracking.entity.Challenge;
import de.thi.mynd.progressTracking.entity.ChallengeType;
import de.thi.mynd.progressTracking.repository.ChallengeRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@ApplicationScoped
public class ChallengeServiceImpl implements ChallengeService {

    private static final int WEEKLY_TARGET = 7;
    private static final int REWARD_INVITATIONS = 2;

    @Inject ChallengeRepository challengeRepository;
    @Inject SecurityIdentity identity;
    @Inject UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public ChallengeDto getCurrentChallenge() {
        String creatorId = identity.getPrincipal().getName();
        LocalDate today = LocalDate.now();
        Challenge challenge = challengeRepository
                .findCurrentForUser(creatorId, ChallengeType.WEEKLY, today)
                .orElseGet(() -> createWeeklyChallenge(creatorId, today));
        return toDto(challenge);
    }

    @Override
    @Transactional
    public void trackContentElementCompletion() {
        String creatorId = identity.getPrincipal().getName();
        LocalDate today = LocalDate.now();
        challengeRepository.findCurrentForUser(creatorId, ChallengeType.WEEKLY, today)
                .ifPresent(challenge -> {
                    if (!challenge.completed) {
                        challenge.currentCount++;
                        if (challenge.currentCount >= challenge.targetCount) {
                            challenge.completed = true;
                        }
                        challengeRepository.persistAndFlush(challenge);
                    }
                });
    }

    @Override
    @Transactional
    public ChallengeDto claimReward() {
        String creatorId = identity.getPrincipal().getName();
        LocalDate today = LocalDate.now();
        Challenge challenge = challengeRepository
                .findCurrentForUser(creatorId, ChallengeType.WEEKLY, today)
                .orElseThrow(() -> new BadRequestException("No active challenge found"));
        if (!challenge.completed) throw new BadRequestException("Challenge not completed yet");
        if (challenge.rewardClaimed) throw new BadRequestException("Reward already claimed");
        challenge.rewardClaimed = true;
        challengeRepository.persistAndFlush(challenge);
        CreatorIdKey key = new CreatorIdKey();
        key.creatorId = creatorId;
        UserProfile profile = userProfileRepository.findById(key);
        profile.invitationsLeft += REWARD_INVITATIONS;
        userProfileRepository.persistAndFlush(profile);
        return toDto(challenge);
    }

    @Override
    public List<ChallengeDto> getChallengeHistory() {
        String creatorId = identity.getPrincipal().getName();
        return challengeRepository
                .findHistoryForUser(creatorId, LocalDate.now())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Challenge createWeeklyChallenge(String creatorId, LocalDate today) {
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Challenge challenge = new Challenge();
        challenge.creatorId = creatorId;
        challenge.type = ChallengeType.WEEKLY;
        challenge.startDate = start;
        challenge.endDate = start.plusDays(6);
        challenge.targetCount = WEEKLY_TARGET;
        challengeRepository.persistAndFlush(challenge);
        return challenge;
    }

    private ChallengeDto toDto(Challenge c) {
        return new ChallengeDto(c.id, c.type, c.startDate, c.endDate,
                c.targetCount, c.currentCount, c.completed, c.rewardClaimed);
    }
}