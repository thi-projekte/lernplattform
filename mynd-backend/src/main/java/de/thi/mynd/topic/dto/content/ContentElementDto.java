package de.thi.mynd.topic.dto.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = PdfElementDto.class, name = "PDF"),
  @JsonSubTypes.Type(value = VideoFileElementDto.class, name = "VIDEO_FILE"),
  @JsonSubTypes.Type(value = AudioFileElementDto.class, name = "AUDIO_FILE"),
  @JsonSubTypes.Type(value = YouTubeLinkElementDto.class, name = "YOUTUBE_LINK"),
  @JsonSubTypes.Type(value = SpotifyLinkElementDto.class, name = "SPOTIFY_LINK"),
  @JsonSubTypes.Type(value = RtfElementDto.class, name = "RTF"),
  @JsonSubTypes.Type(value = UriElementDto.class, name = "URI"),
  @JsonSubTypes.Type(value = ImageElementDto.class, name = "IMAGE")
})
@SuperBuilder
public abstract class ContentElementDto {

  public UUID id;

  public LocalDateTime createdAt;

  public LocalDateTime updatedAt;

  public String title;

  public String icon;

  public Integer rank;
}
