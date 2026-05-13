package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "learn_progress_topic")
public class LearnProgressTopic extends BaseEntity {

    @EmbeddedId
    @AttributeOverride(name = "creatorId", column = @Column(name = "creatorId", insertable = false, updatable = false))
    public LearnProgressTopicId id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public LearnProgressStatus status;

    @OneToMany(mappedBy = "progressTopic")
    public List<LearnProgressContentElement> contentElements;
}
