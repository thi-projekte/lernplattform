package de.thi.mynd.topic.entity;

public enum ContentType {
  PDF("PDF"),
  VIDEO_FILE("VIDEO_FILE"),
  AUDIO_FILE("AUDIO_FILE"),
  YOUTUBE_LINK("YOUTUBE_LINK"),
  SPOTIFY_LINK("SPOTIFY_LINK"),
  RTF("RTF"),
  URI("URI"),
  IMAGE("IMAGE");

  public final String label;

  private ContentType(String label) {
    this.label = label;
  }
}
