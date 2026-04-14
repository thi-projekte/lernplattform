package de.thi.mynd.common.dto;

import lombok.Builder;

import java.util.List;

@Builder
public final class PaginationDto<T> {

    public List<T> results;

    public int totalPages;

    public boolean hasNextPage;

    public boolean hasPreviousPage;
}
