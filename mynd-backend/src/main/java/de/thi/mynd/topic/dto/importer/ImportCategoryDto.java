package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public final class ImportCategoryDto {
  @NotBlank public String identifier;
  @NotBlank public String title;
  @NotBlank public String color;
}
