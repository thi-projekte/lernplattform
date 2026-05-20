package de.thi.mynd.auth.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseEntityWithCreatorIdPk {

  @Column(nullable = true)
  public String profilePictureKey;

  @Column(nullable = false, name = "invitationsLeft")
  public int invitationsLeft;
}
