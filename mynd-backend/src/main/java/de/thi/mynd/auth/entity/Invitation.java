package de.thi.mynd.auth.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "invitation")
public class Invitation extends BaseEntityWithId {

  @Column public String acceptedBy;

  @Column(nullable = false)
  public String mailSentTo;

  @Column(nullable = false)
  public String redemptionSecret;
}
