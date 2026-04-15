package de.thi.mynd.common.repository;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.entity.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.List;

public abstract class MyndBaseRepository<T extends BaseEntity> implements PanacheRepository<T> {

  public List<T> findAllWithLimit(int limit) {
    return findAll().range(0, limit).list();
  }

  protected PaginationDto<T> buildPaginationFromQuery(
      PanacheQuery<T> query, int page, int pageSize) {
    query.page(page, pageSize);
    return PaginationDto.<T>builder()
        .results(query.list())
        .totalPages(query.pageCount())
        .build();
  }
}
