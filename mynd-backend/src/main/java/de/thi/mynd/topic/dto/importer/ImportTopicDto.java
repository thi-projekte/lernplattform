package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public final class ImportTopicDto {

  @NotBlank public String identifier;
  @NotBlank public String title;
  @NotBlank public String teaser;

  @Size(min = 1, max = 2)
  public List<String> categories;

  @NotNull public Integer duration;

  @Size(min = 1, max = 12)
  public List<Map<String, Object>> contentElements;
}
