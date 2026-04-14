package de.thi.mynd.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    public LocalDateTime updatedAt;
}
