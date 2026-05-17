package de.thi.mynd.common.entity;

import jakarta.persistence.*;

@MappedSuperclass
@AttributeOverride(
        name = "creatorId",
        column = @Column(name = "creatorId", insertable = false, updatable = false))
public abstract class BaseEntityWithCreatorIdPk extends BaseEntity {

  @EmbeddedId
  CreatorIdKey id;
}
