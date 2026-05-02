package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rtf_element")
@DiscriminatorValue("RTF")
public class RtfElement extends ContentElement {

  @Column(nullable = false, columnDefinition = "TEXT")
  public String rtfText;
}
