/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.request.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = PdfElementRequest.class, name = "PDF"),
  @JsonSubTypes.Type(value = VideoFileElementRequest.class, name = "VIDEO_FILE"),
  @JsonSubTypes.Type(value = AudioFileElementRequest.class, name = "AUDIO_FILE"),
  @JsonSubTypes.Type(value = YouTubeLinkElementRequest.class, name = "YOUTUBE_LINK"),
  @JsonSubTypes.Type(value = SpotifyLinkElementRequest.class, name = "SPOTIFY_LINK"),
  @JsonSubTypes.Type(value = RtfElementRequest.class, name = "RTF"),
  @JsonSubTypes.Type(value = UriElementRequest.class, name = "URI"),
  @JsonSubTypes.Type(value = ImageElementRequest.class, name = "IMAGE"),
})
public abstract class ContentElementRequest {

  @NotNull @NotBlank public String title;

  @NotNull @NotBlank public String icon;
}
