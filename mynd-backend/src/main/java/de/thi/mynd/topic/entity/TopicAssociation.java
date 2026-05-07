package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "topic_association")
public class TopicAssociation extends BaseEntity {

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "owning_topic_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Topic owningTopic;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "foreign_topic_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Topic foreignTopic;
}
