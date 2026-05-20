package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;

@RegisterForReflection
@Getter
public final class ImportCategoryDto {
  public String identifier;
  public String title;
  public String color;
}
