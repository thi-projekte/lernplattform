package de.thi.mynd.topic.dto.content;

import lombok.experimental.SuperBuilder;

import java.net.URL;

@SuperBuilder
public final class PdfElementDto extends ContentElementDto {

    public URL presignedUrl;

    public String originalFileName;
}
