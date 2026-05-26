package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "streak_continuation")
public class StreakContinuation extends BaseEntityWithId {

    @ManyToMany(mappedBy = "continuations")
    public List<Streak> streaks;
}
