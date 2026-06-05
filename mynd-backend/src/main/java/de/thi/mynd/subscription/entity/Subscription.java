package de.thi.mynd.subscription.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription")
public class Subscription extends BaseEntityWithCreatorIdPk {

  @Column public String stripeCustomerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public SubscriptionStatus subscriptionStatus;

  @Column(nullable = false)
  public boolean usedTrial;

  @Column public String stripeSubscriptionId;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "features", columnDefinition = "text[]")
  public List<String> features = new ArrayList<>();
}
