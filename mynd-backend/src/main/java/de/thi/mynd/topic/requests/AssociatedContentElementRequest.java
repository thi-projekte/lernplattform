package de.thi.mynd.topic.requests;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import jakarta.validation.constraints.Min;

public final class AssociatedContentElementRequest extends AssociatedEntityRequest {

  @Min(0)
  public Integer rank;
}
