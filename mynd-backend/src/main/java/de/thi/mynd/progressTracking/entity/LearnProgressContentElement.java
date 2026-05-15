package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "learn_progress_content_element")
@AttributeOverride(
        name = "creatorId",
        column = @Column(name = "creatorId", insertable = false, updatable = false))
public class LearnProgressContentElement extends BaseEntity {

  @EmbeddedId
  public LearnProgressContentElementId id;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(
        name = "creatorId",
        referencedColumnName = "creatorId",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "topicId",
        referencedColumnName = "topicId",
        insertable = false,
        updatable = false)
  })
  public LearnProgressTopic progressTopic;

  @Column(nullable = false)
  public boolean completed;
}
