package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "uri_element")
@DiscriminatorValue("URI")
public class UriElement extends ContentElement {

  @Column(nullable = false)
  public String uri;
}
