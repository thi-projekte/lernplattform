package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ContentElement extends BaseEntity {

  @Column(nullable = false)
  public String title;

  @Column(name = "type", insertable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  public ContentType type;

  @Column(name = "rank", nullable = true)
  public Integer rank;

  @ManyToOne
  @JoinColumn(name = "topic_id")
  public Topic topic;
}
