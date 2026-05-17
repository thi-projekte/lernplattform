package de.thi.mynd.auth.entity;

import de.thi.mynd.common.entity.BaseEntity;
import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseEntityWithCreatorIdPk {

  public String profilePictureKey;
}
