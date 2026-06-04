package de.thi.mynd.progressTracking.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "challenge")
public class Challenge extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String creatorId;

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