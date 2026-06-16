/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Valid;
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
public final class FullImportDto {
  @NotNull private List<@Valid ImportTopicDto> topics;

  @NotNull
  private Map<@NotBlank String, @Size(min = 1, max = 3) List<@NotBlank String>> associations;
}
