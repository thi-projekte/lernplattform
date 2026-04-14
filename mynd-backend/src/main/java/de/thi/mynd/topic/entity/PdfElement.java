package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PDF")
public class PdfElement extends ContentElement {

    @Column(nullable = false)
    public String s3Key;

    @Column
    public String fileSize;

    @Column(nullable = false)
    public String originalFileName;
}
