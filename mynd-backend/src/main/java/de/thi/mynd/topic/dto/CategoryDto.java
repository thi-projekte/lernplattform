package de.thi.mynd.topic.dto;

import java.util.UUID;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CategoryDto {
  public UUID id;
  public String title;
  public String color;
}
