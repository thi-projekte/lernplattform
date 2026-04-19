package de.thi.mynd.topic.requests.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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

  public String title;
}
