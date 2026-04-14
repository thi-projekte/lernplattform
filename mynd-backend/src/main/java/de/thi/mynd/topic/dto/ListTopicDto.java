package de.thi.mynd.topic.dto;

import de.thi.mynd.topic.entity.Category;
import de.thi.mynd.topic.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public class ListTopicDto {

  public String title;

  public List<Category> categories;

  public LocalDateTime updatedAt;

  public static ListTopicDto from(Topic topic) {
    return ListTopicDto.builder()
        .title(topic.title)
        .categories(topic.categories)
        .updatedAt(topic.updatedAt)
        .build();
  }
}
