package de.thi.mynd.auth.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_profile")
@AttributeOverride(
        name = "creatorId",
        column = @Column(name = "creatorId", insertable = false, updatable = false))
public class UserProfile extends BaseEntity {

  @Id
  public String creatorId;

  public String profilePictureKey;
}
