package de.thi.mynd.topic.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SPOTIFY_LINK")
public class SpotifyLinkElement extends ContentElement{
}
