package de.thi.mynd.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SPOTIFY_LINK")
public class SpotifyLinkElement extends ContentElement {

    @Column(nullable = false)
    public String uri;
}
