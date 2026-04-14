package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "content_element")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ContentElement extends BaseEntity {

  @Column(nullable = false)
  public String title;

  @Column(name = "type", insertable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  public ContentType type;

  @ManyToOne
  @JoinColumn(name = "topic_id")
  public Topic topic;
}
