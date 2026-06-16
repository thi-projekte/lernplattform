/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.exception.FeatureQuotaHitException;
import de.thi.mynd.subscription.repository.FeatureQuotaRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FeatureQuotaActionsServiceImplTest {

  static {
    System.setProperty("mynd.subsciptionFeatures.freeParallelTopicLimit", "2");
    System.setProperty("mynd.subsciptionFeatures.freeDailyLearnLimit", "3");
  }

  @Inject FeatureQuotaActionsServiceImpl service;

  @InjectMock SubscriptionService subscriptionService;

  @InjectMock FeatureQuotaRepository featureQuotaRepository;

  private static final String USER_ID = "user-123";
  private FeatureQuota mockQuota;
  private Subscription mockSubscription;

  @BeforeEach
  void setUp() {
    mockQuota = new FeatureQuota();
    mockQuota.count = 0;

    mockSubscription = new Subscription();
    mockSubscription.features = new ArrayList<>();

    when(subscriptionService.getSubscriptionForUser(USER_ID)).thenReturn(mockSubscription);
  }

  // =========================================================================
  // learnContentElement() Tests
  // =========================================================================

  @Test
  void learnContentElement_WithinFreeLimit_ShouldIncrementAndPersist()
      throws FeatureQuotaHitException {
    mockQuota.count = 2; // Increments to 3 (<= 3 limit)

    // Mock repository behavior instead of service method spy
    when(featureQuotaRepository.findByCreatorAndFeatureAndDate(
            eq(USER_ID), eq(Feature.LearnContentElementOrTopic), any(LocalDate.class)))
        .thenReturn(Optional.of(mockQuota));

    assertDoesNotThrow(() -> service.learnContentElement(USER_ID));

    assertEquals(3, mockQuota.count);
    // Persist called inside the service method
    verify(featureQuotaRepository, times(1)).persistAndFlush(mockQuota);
  }

  @Test
  void learnContentElement_ExceedsFreeLimitWithoutPremium_ShouldThrowException() {
    mockQuota.count = 3; // Increments to 4 (> 3 limit)

    when(featureQuotaRepository.findByCreatorAndFeatureAndDate(
            eq(USER_ID), eq(Feature.LearnContentElementOrTopic), any(LocalDate.class)))
        .thenReturn(Optional.of(mockQuota));

    FeatureQuotaHitException exception =
        assertThrows(
            FeatureQuotaHitException.class,
            () -> {
              service.learnContentElement(USER_ID);
            });

    assertEquals("You cannot learn more content elements today", exception.getMessage());
    // Should only be persisted once if fallback happened, but service execution should halt before
    // saving bad state
    verify(featureQuotaRepository, never()).persistAndFlush(mockQuota);
  }

  @Test
  void learnContentElement_FallbackToNewQuota_CreatesSuccessfully()
      throws FeatureQuotaHitException {
    // Mock database returning empty (no existing quota found for today or ever)
    when(featureQuotaRepository.findByCreatorAndFeatureAndDate(any(), any(), any()))
        .thenReturn(Optional.empty());
    when(featureQuotaRepository.findByCreatorAndFeature(any(), any())).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> service.learnContentElement(USER_ID));

    // Abstract class creates new record (count = 0), then action increments it to 1
    verify(featureQuotaRepository, times(2)).persistAndFlush(any(FeatureQuota.class));
  }

  // =========================================================================
  // completeTopic() Tests
  // =========================================================================

  @Test
  void completeTopic_CountGreaterThanZero_ShouldDecrementAndPersist()
      throws FeatureQuotaHitException {
    mockQuota.count = 2;
    when(featureQuotaRepository.findByCreatorAndFeature(USER_ID, Feature.StartTopic))
        .thenReturn(Optional.of(mockQuota));

    assertDoesNotThrow(() -> service.completeTopic(USER_ID));

    assertEquals(1, mockQuota.count);
    verify(featureQuotaRepository, times(2)).persistAndFlush(mockQuota);
  }

  // =========================================================================
  // startNewTopic() Tests
  // =========================================================================

  @Test
  void startNewTopic_ExceedsFreeLimitWithoutPremium_ShouldThrowException() {
    mockQuota.count = 2; // Increments to 3 (> 2 limit)
    when(featureQuotaRepository.findByCreatorAndFeature(USER_ID, Feature.StartTopic))
        .thenReturn(Optional.of(mockQuota));

    FeatureQuotaHitException exception =
        assertThrows(
            FeatureQuotaHitException.class,
            () -> {
              service.startNewTopic(USER_ID);
            });

    assertEquals("You cannot have that many topics running in parallel", exception.getMessage());
  }
}
