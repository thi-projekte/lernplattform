/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.dto;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.entity.Category;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TopicDto {

  public UUID id;
  public String title;
  public String teaser;
  public LocalDateTime updatedAt;
  public String creatorId;
  public String creatorFullName;
  public List<Category> categories;
  public int estimatedLearningDuration;
  public List<ContentElementDto> contentElements;
  public List<IndexCardDto> indexCards;
  public TopicLearnProgressDto learnProgress;
}
