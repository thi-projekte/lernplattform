/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.dto.graph;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.topic.entity.Category;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public final class GraphTopicDto {
  public UUID id;

  public String title;

  public List<Category> categories;

  public LocalDateTime updatedAt;

  public String creatorId;

  public String creatorFullName;

  public List<UUID> associatedTopics;

  public TopicLearnProgressDto learnProgress;

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GraphTopicDto dto) {
      return dto.id.equals(this.id);
    }
    return false;
  }
}
