package de.thi.mynd.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
public abstract class BaseEntity {

  @Id @GeneratedValue public UUID id;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  public LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public LocalDateTime updatedAt;
}
