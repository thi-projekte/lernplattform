package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "youtube_link_element")
@DiscriminatorValue("YOUTUBE_LINK")
public class YouTubeLinkElement extends ContentElement {

  @Column(nullable = false)
  public String uri;
}
