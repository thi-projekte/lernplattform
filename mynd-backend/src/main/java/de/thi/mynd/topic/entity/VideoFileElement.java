/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
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
