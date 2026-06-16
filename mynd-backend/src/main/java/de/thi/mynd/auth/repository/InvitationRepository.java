/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.auth.repository;

import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.repository.MyndBaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class InvitationRepository extends MyndBaseRepository<Invitation> {

  public PaginationDto<Invitation> getInvitationsForCreatorPaginated(
      String creatorId, int page, int pageSize) {
    return buildPaginationFromQuery(find("creatorId = ?1", creatorId), page, pageSize);
  }

  public long getAmountInvitationsSubmittedPerUser(String username) {
    return find("creatorId = ?1", username).count();
  }
}
