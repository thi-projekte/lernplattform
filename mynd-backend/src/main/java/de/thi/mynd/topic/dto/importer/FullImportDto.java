package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public final class FullImportDto {
  @NotNull
  private List<@Valid ImportTopicDto> topics;
  @NotNull
  private Map<@NotBlank String, @Size(min = 1, max = 3) List<@NotBlank String>> associations;
}
