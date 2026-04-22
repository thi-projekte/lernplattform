package de.thi.mynd.topic.entity;

import de.thi.mynd.common.service.FileAssociatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "video_file_element")
@DiscriminatorValue("VIDEO_FILE")
public class VideoFileElement extends ContentElement implements FileAssociatedEntity {

  @Column(nullable = false)
  public String s3Key;

  @Column(nullable = false)
  public String originalFileName;

  @Override
  public List<String> getFileKeys() {
    return List.of(s3Key);
  }
}
