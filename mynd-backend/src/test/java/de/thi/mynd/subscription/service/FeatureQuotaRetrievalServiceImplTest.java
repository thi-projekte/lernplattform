package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.thi.mynd.subscription.StripeFeatureFlagConstants;
import de.thi.mynd.subscription.entity.Feature;
import de.thi.mynd.subscription.entity.FeatureQuota;
import de.thi.mynd.subscription.entity.Subscription;
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
class FeatureQuotaRetrievalServiceImplTest {

  static {
    System.setProperty("mynd.subsciptionFeatures.freeParallelTopicLimit", "2");
    System.setProperty("mynd.subsciptionFeatures.freeDailyLearnLimit", "3");
  }

  @Inject FeatureQuotaRetrievalServiceImpl service;

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
  // canLearn() Tests
  // =========================================================================

  @Test
  void canLearn_BelowLimitWithoutPremium_ShouldReturnTrue() {
    mockQuota.count = 2; // 2 < 3
    when(featureQuotaRepository.findByCreatorAndFeatureAndDate(
            eq(USER_ID), eq(Feature.LearnContentElementOrTopic), any(LocalDate.class)))
        .thenReturn(Optional.of(mockQuota));

    assertTrue(service.canLearn(USER_ID));
  }

  @Test
  void canLearn_AtLimitWithoutPremium_ShouldReturnFalse() {
    mockQuota.count = 3; // 3 is not < 3
    when(featureQuotaRepository.findByCreatorAndFeatureAndDate(
            eq(USER_ID), eq(Feature.LearnContentElementOrTopic), any(LocalDate.class)))
        .thenReturn(Optional.of(mockQuota));

    assertFalse(service.canLearn(USER_ID));
  }

  @Test
  void canLearn_AboveLimitWithPremium_ShouldReturnTrue() {
    mockQuota.count = 5;
    mockSubscription.features.add(StripeFeatureFlagConstants.UnlimitedLearning);
    when(featureQuotaRepository.findByCreatorAndFeatureAndDate(
            eq(USER_ID), eq(Feature.LearnContentElementOrTopic), any(LocalDate.class)))
        .thenReturn(Optional.of(mockQuota));

    assertTrue(service.canLearn(USER_ID));
  }

  // =========================================================================
  // canStartNewTopic() Tests
  // =========================================================================

  @Test
  void canStartNewTopic_AtLimitWithoutPremium_ShouldReturnFalse() {
    mockQuota.count = 2; // 2 is not < 2
    when(featureQuotaRepository.findByCreatorAndFeature(USER_ID, Feature.StartTopic))
        .thenReturn(Optional.of(mockQuota));

    assertFalse(service.canStartNewTopic(USER_ID));
  }

  @Test
  void canStartNewTopic_AboveLimitWithPremium_ShouldReturnTrue() {
    mockQuota.count = 4;
    mockSubscription.features.add(StripeFeatureFlagConstants.UnlimitedParallelTopics);
    when(featureQuotaRepository.findByCreatorAndFeature(USER_ID, Feature.StartTopic))
        .thenReturn(Optional.of(mockQuota));

    assertTrue(service.canStartNewTopic(USER_ID));
  }
}
