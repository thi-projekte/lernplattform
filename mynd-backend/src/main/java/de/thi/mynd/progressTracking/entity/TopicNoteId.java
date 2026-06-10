package de.thi.mynd.progressTracking.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class TopicNoteId implements Serializable {
    public UUID topicId;
    public String creatorId;
}
