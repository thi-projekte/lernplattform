package de.thi.mynd.common.entity;

import de.thi.mynd.common.security.EntityOwnerPrePersistListener;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@EntityListeners(EntityOwnerPrePersistListener.class)
public abstract class BaseEntity {

  @Column(nullable = false, name = "creatorId")
  public String creatorId;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  public LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public LocalDateTime updatedAt;
}
