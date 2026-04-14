package de.thi.mynd.topic.entity;

public enum ContentType {
    Pdf("PDF"),
    VideoFile("VIDEO_FILE"),
    AudioFile("AUDIO_FILE"),
    YouTubeLink("YOUTUBE_LINK"),
    SpotifyLink("SPOTIFY_LINK"),
    Rtf("RTF"),
    Uri("URI"),
    Image("IMAGE");

    public final String label;

    private ContentType(String label) {
        this.label = label;
    }
}
