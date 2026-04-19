package de.thi.mynd.topic.requests;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class AssociatedContentElementRequest extends AssociatedEntityRequest {

  @NotBlank
  @Min(0)
  public Integer rank;
}
