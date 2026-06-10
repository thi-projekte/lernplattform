package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "topic_note")
@AttributeOverride(
        name = "creatorId",
        column = @Column(name = "creatorId", insertable = false, updatable = false))
public class TopicNote extends BaseEntity {

    @EmbeddedId
    public TopicNoteId id;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;
}
