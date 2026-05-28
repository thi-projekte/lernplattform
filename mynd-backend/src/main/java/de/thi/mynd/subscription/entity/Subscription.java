package de.thi.mynd.subscription.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

@Entity
@Table(name = "subscription")
public class Subscription extends BaseEntityWithCreatorIdPk {

    @Column
    public String stripeCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SubscriptionStatus subscriptionStatus;

    @Column
    public String stripeSubscriptionId;
}
