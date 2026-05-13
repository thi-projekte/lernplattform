package de.thi.mynd.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntityWithId extends BaseEntity {

  @Id @GeneratedValue public UUID id;
}
