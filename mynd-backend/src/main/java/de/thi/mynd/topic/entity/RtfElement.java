package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RTF")
public class RtfElement extends ContentElement {

    @Column(nullable = false, columnDefinition = "TEXT")
    public String rtfText;
}
