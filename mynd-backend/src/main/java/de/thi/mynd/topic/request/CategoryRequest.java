package de.thi.mynd.topic.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class CategoryRequest {

  @NotBlank public String title;

  @NotBlank public String color;

  public UUID parentId;
}
