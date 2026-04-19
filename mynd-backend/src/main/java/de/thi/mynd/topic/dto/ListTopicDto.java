package de.thi.mynd.topic.dto;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public final class ListTopicDto {

  public UUID id;

  public String title;

  public List<Category> categories;

  public LocalDateTime updatedAt;

  public static ListTopicDto from(Topic topic) {
    return ListTopicDto.builder()
        .id(topic.id)
        .title(topic.title)
        .categories(topic.categories)
        .updatedAt(topic.updatedAt)
        .build();
  }
}
