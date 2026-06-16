/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.dto;

import java.util.List;
import lombok.Builder;

@Builder
public final class PaginationDto<T> {

  public List<T> results;

  public int totalPages;
}
