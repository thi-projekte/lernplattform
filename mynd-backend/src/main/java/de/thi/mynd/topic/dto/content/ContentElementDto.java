package de.thi.mynd.topic.dto.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.thi.mynd.topic.entity.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PdfElement.class, name = "PDF"),
        @JsonSubTypes.Type(value = VideoFileElement.class, name = "VIDEO_FILE"),
        @JsonSubTypes.Type(value = AudioFileElement.class, name = "AUDIO_FILE"),
        @JsonSubTypes.Type(value = YouTubeLinkElement.class, name = "YOUTUBE_LINK"),
        @JsonSubTypes.Type(value = SpotifyLinkElement.class, name = "SPOTIFY_LINK"),
        @JsonSubTypes.Type(value = RtfElement.class, name = "RTF"),
        @JsonSubTypes.Type(value = UriElement.class, name = "URI"),
        @JsonSubTypes.Type(value = ImageElement.class, name = "IMAGE")
})
public abstract class ContentElementDto {

    public String title;
}
