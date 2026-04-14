package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("IMAGE")
public class ImageElement extends ContentElement {

    @Column(nullable = false)
    public String s3Key;

    @Column(nullable = false)
    public String originalFileName;
}
