/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.common.repository;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.entity.BaseEntityWithId;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.util.List;
import java.util.UUID;

public abstract class MyndBaseRepository<T extends BaseEntityWithId>
    implements PanacheRepositoryBase<T, UUID> {

  public List<T> findAllWithLimit(int limit) {
    return findAll().range(0, limit).list();
  }

  protected PaginationDto<T> buildPaginationFromQuery(
      PanacheQuery<T> query, int page, int pageSize) {
    query.page(page, pageSize);
    return PaginationDto.<T>builder().results(query.list()).totalPages(query.pageCount()).build();
  }

  public List<T> findByIdsTypeSafe(List<UUID> ids) {
    return find("id IN ?1", ids).list();
  }
}
