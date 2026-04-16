package de.thi.mynd.topic.dto.content;

import lombok.Builder;

@Builder
public final class ImageElementDto extends ContentElementDto {

    public String presignedUrl;

    public String originalFileName;
}
