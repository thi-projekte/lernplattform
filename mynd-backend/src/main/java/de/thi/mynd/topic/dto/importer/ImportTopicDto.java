/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

  public int duration;

  @Size(min = 1, max = 12)
  public List<Map<String, Object>> contentElements;

  @Size(max = 20)
  public List<@Valid ImportIndexCardDto> indexCards;
}
