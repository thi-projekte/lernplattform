package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

@Entity
@Table(name = "streak_preference")
public class StreakPreference extends BaseEntityWithCreatorIdPk {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public StreakType type;

    @Column(nullable = false)
    public boolean isPublic;
}
