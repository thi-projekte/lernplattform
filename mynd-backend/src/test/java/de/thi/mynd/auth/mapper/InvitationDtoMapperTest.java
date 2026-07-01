/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.entity.Invitation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InvitationDtoMapperTest {

  @Inject InvitationDtoMapper invitationDtoMapper;

  private Invitation invitation() {
    Invitation invitation = new Invitation();
    invitation.id = UUID.randomUUID();
    invitation.creatorId = "alice";
    invitation.mailSentTo = "bob@example.com";
    invitation.createdAt = LocalDateTime.now();
    return invitation;
  }

  @Test
  void mapAndEnrich_copiesAllScalarFields() {
    Invitation invitation = invitation();

    InvitationDto dto = invitationDtoMapper.mapAndEnrich(invitation);

    assertEquals(invitation.id, dto.id);
    assertEquals(invitation.creatorId, dto.creatorId);
    assertEquals(invitation.mailSentTo, dto.mailSentTo);
    assertEquals(invitation.createdAt, dto.createdAt);
  }

  @Test
  void mapAndEnrich_acceptedByNull_acceptedIsFalse() {
    Invitation invitation = invitation();
    invitation.acceptedBy = null;

    InvitationDto dto = invitationDtoMapper.mapAndEnrich(invitation);

    assertFalse(dto.accepted);
  }

  @Test
  void mapAndEnrich_acceptedByPresent_acceptedIsTrue() {
    Invitation invitation = invitation();
    invitation.acceptedBy = "carol";

    InvitationDto dto = invitationDtoMapper.mapAndEnrich(invitation);

    assertTrue(dto.accepted);
  }

  @Test
  void getEntityType_returnsInvitation() {
    assertEquals(Invitation.class, invitationDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsInvitationDto() {
    assertEquals(InvitationDto.class, invitationDtoMapper.getDtoType());
  }
}
