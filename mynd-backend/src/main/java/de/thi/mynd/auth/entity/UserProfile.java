package de.thi.mynd.auth.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseEntity {

  public String profilePictureKey;
}