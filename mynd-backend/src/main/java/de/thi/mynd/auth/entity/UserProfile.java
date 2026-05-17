package de.thi.mynd.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_profile")
public class UserProfile {

  @Id public String creatorId;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  public LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public LocalDateTime updatedAt;

  public String profilePictureKey;
}