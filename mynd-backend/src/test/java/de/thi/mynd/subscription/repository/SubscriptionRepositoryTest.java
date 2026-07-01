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

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.subscription.entity.Subscription;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link SubscriptionRepository} against a real Postgres instance (Quarkus dev services).
 * Every fixture creatorId is randomized to avoid primary key collisions and false positives from
 * pre-existing rows.
 */
@QuarkusTest
class SubscriptionRepositoryTest {

  @Inject SubscriptionRepository subscriptionRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private Subscription newSubscription(String creatorId) {
    Subscription subscription = new Subscription();
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = creatorId;
    subscription.id = id;
    subscription.creatorId = creatorId;
    subscription.subscriptionStatus = SubscriptionStatus.FREE;
    subscription.usedTrial = false;
    return subscription;
  }

  @Test
  @TestTransaction
  void findByStripeSubscriptionId_existingId_returnsSubscription() {
    String creatorId = unique("creator");
    String stripeSubscriptionId = unique("sub");
    Subscription subscription = newSubscription(creatorId);
    subscription.stripeSubscriptionId = stripeSubscriptionId;
    subscriptionRepository.persistAndFlush(subscription);

    Optional<Subscription> result =
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);

    assertTrue(result.isPresent());
    assertEquals(creatorId, result.get().id.creatorId);
  }

  @Test
  @TestTransaction
  void findByStripeSubscriptionId_noMatch_returnsEmpty() {
    Optional<Subscription> result =
        subscriptionRepository.findByStripeSubscriptionId(unique("missing-sub"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByStripeCustomerId_existingId_returnsSubscription() {
    String creatorId = unique("creator");
    String stripeCustomerId = unique("cus");
    Subscription subscription = newSubscription(creatorId);
    subscription.stripeCustomerId = stripeCustomerId;
    subscriptionRepository.persistAndFlush(subscription);

    Optional<Subscription> result = subscriptionRepository.findByStripeCustomerId(stripeCustomerId);

    assertTrue(result.isPresent());
    assertEquals(creatorId, result.get().id.creatorId);
  }

  @Test
  @TestTransaction
  void findByStripeCustomerId_noMatch_returnsEmpty() {
    Optional<Subscription> result =
        subscriptionRepository.findByStripeCustomerId(unique("missing-cus"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findByIdsTypeSafe_inheritedFromCustomIdRepository_isUnusableForCompositeIdEntities() {
    // MyndBaseCustomIdRepository.findByIdsTypeSafe hardcodes List<UUID> as the parameter type, but
    // every current subclass (including this one) keys its entity with an @EmbeddedId
    // (CreatorIdKey), so "id IN ?1" can never bind against a UUID list. This documents that the
    // inherited method is effectively dead/broken for this repository rather than silently leaving
    // it untested.
    assertThrows(
        Exception.class,
        () -> subscriptionRepository.findByIdsTypeSafe(List.of(UUID.randomUUID())));
  }
}
