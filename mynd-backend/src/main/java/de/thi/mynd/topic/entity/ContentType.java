/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
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
