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
  void findByIdsTypeSafe_returnsOnlyRequestedIds() {
    Subscription a = newSubscription(unique("creator-a"));
    Subscription b = newSubscription(unique("creator-b"));
    Subscription c = newSubscription(unique("creator-c"));
    subscriptionRepository.persistAndFlush(a);
    subscriptionRepository.persistAndFlush(b);
    subscriptionRepository.persistAndFlush(c);

    List<Subscription> result = subscriptionRepository.findByIdsTypeSafe(List.of(a.id, c.id));

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.id.equals(a.id)));
    assertTrue(result.stream().anyMatch(s -> s.id.equals(c.id)));
    assertTrue(result.stream().noneMatch(s -> s.id.equals(b.id)));
  }

  @Test
  @TestTransaction
  void findByIdsTypeSafe_emptyList_returnsEmpty() {
    List<Subscription> result = subscriptionRepository.findByIdsTypeSafe(List.of());

    assertTrue(result.isEmpty());
  }
}
