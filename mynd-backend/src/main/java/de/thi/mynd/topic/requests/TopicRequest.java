package de.thi.mynd.topic.requests;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public final class TopicRequest {
  @NotBlank public String title;

  @NotBlank
  @Size(max = 512)
  public String teaser;

  @Size(min = 1, max = 3)
  public List<@Valid AssociatedEntityRequest> categories;

  @NotNull public int estimatedLearningDuration;

  @Size(min = 1, max = 4)
  public List<@Valid AssociatedEntityRequest> relatedTopics;

  @Size(min = 1)
  public List<@Valid AssociatedContentElementRequest> contentElements;
}
