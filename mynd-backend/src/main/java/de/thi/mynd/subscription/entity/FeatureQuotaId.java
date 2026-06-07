package de.thi.mynd.subscription.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class FeatureQuotaId implements Serializable {

  public String creatorId;

  @Enumerated(EnumType.STRING)
  public Feature feature;
}
