/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.mapper;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.common.processor.AbstractMappingProcessor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class InvitationDtoMapper extends AbstractMappingProcessor<Invitation, InvitationDto> {
  @Override
  public InvitationDto mapAndEnrich(Invitation entity) {
    return InvitationDto.builder()
        .id(entity.id)
        .createdAt(entity.createdAt)
        .creatorId(entity.creatorId)
        .mailSentTo(entity.mailSentTo)
        .accepted(entity.acceptedBy != null)
        .build();
  }

  @Override
  public Class<Invitation> getEntityType() {
    return Invitation.class;
  }

  @Override
  public Class<InvitationDto> getDtoType() {
    return InvitationDto.class;
  }
}
