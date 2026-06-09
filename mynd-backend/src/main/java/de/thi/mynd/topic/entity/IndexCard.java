package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "index_card")
public class IndexCard extends BaseEntityWithId {

    @ManyToOne
    @JoinColumn(
            name = "topicId",
            referencedColumnName = "id",
            nullable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Topic topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String answer;
}
