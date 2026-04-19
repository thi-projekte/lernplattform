package de.thi.mynd.common.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AssociatedEntityRequest {

  @NotNull
  public UUID id;
}
