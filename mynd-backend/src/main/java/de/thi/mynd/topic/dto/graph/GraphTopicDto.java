package de.thi.mynd.topic.dto.graph;

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
}
