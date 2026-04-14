package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AUDIO_FILE")
public class AudioFileElement extends ContentElement {

    @Column(nullable = false)
    public String s3Key;

    @Column
    public String fileSize;

    @Column(nullable = false)
    public String originalFileName;
}
