package de.thi.mynd.subscription.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "feature_quota_tracking")
public class FeatureQuota extends BaseEntityWithCreatorIdPk {

  @Column(nullable = false)
  public LocalDate dayAccountedFor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Feature feature;

  @Column(nullable = false)
  public int count = 0;
}
