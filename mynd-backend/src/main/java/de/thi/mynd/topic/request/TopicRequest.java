/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.request;

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

  @Size(min = 1, max = 12)
  public List<@Valid AssociatedContentElementRequest> contentElements;

  @Size(max = 20)
  public List<@Valid AssociatedEntityRequest> indexCards;
}
