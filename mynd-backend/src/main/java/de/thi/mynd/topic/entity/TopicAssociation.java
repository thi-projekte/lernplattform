package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "topic_association")
public class TopicAssociation extends BaseEntity {

  @Column(nullable = false)
  public String creatorId;

  @ManyToOne public Topic owningTopic;

  @ManyToOne public Topic foreignTopic;
}
