package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "streak")
public class Streak extends BaseEntityWithId {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public StreakType type;

    @Column(nullable = false)
    public LocalDateTime startedAt;

    @Column(nullable = true)
    public LocalDateTime endedAt;

    @Column(nullable = false)
    public LocalDateTime lastContinuedAt;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "join_streak_streak_continuation",
            joinColumns = @JoinColumn(name = "streak_id"),
            inverseJoinColumns = @JoinColumn(name = "streak_continuation_id"))
    public List<StreakContinuation> continuations;
}
