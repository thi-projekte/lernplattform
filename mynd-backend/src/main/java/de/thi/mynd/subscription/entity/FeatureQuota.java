package de.thi.mynd.subscription.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "feature_quota_tracking")
public class FeatureQuota extends BaseEntityWithCreatorIdPk {

  @Column(nullable = false)
  public LocalDate dayAccountedFor;

  @Column(nullable = false)
  public String feature;

  @Column(nullable = false)
  public int count = 0;
}
