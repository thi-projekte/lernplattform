/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.subscription.repository;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.subscription.entity.Subscription;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public final class SubscriptionRepository
    extends MyndBaseCustomIdRepository<Subscription, CreatorIdKey> {

  public Optional<Subscription> findByStripeSubscriptionId(String subscriptionId) {
    return find("stripeSubscriptionId = ?1", subscriptionId).firstResultOptional();
  }

  public Optional<Subscription> findByStripeCustomerId(String customerId) {
    return find("stripeCustomerId = ?1", customerId).firstResultOptional();
  }
}
