package de.thi.mynd.common.requests;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class AssociatedEntityRequest {

  @NotBlank public UUID id;
}
