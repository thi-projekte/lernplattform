package de.thi.mynd.progressTracking.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class LearnProgressContentElementId implements Serializable {
    public String creatorId;
    public UUID topicId;
    public UUID contentElementId;
}
