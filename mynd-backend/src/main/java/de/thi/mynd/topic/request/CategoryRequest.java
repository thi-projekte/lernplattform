/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class CategoryRequest {

  @NotBlank public String title;

  @NotBlank public String color;

  public UUID parentId;
}
