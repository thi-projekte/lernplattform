package de.thi.mynd.topic.dto.content;

import java.net.URL;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class ImageElementDto extends ContentElementDto {

  public URL presignedUrl;

  public String originalFileName;
}
