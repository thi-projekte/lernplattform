package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "challenge")
public class Challenge extends BaseEntityWithId {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ChallengeType type;

  @Column(nullable = false)
  public LocalDate startDate;

  @Column(nullable = false)
  public LocalDate endDate;

  @Column(nullable = false)
  public int targetCount;

  @Column(nullable = false)
  public int currentCount = 0;

  @Column(nullable = false)
  public boolean completed = false;

  @Column(nullable = false)
  public boolean rewardClaimed = false;
}
