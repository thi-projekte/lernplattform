package de.thi.mynd.common.dto;

import java.util.List;
import lombok.Builder;

@Builder
public final class PaginationDto<T> {

  public List<T> results;

  public int totalPages;
}
