/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.common.dto.PaginationDto;
import java.util.UUID;

public interface InvitationService {

  InvitationDto getInvitation(UUID invitationId);

  PaginationDto<InvitationDto> getSentInvitations(int page, int pageSize);

  PersonalInvitationStatusDto getPersonalInvitationStatus();

  void sendInvitation(String email);

  void redeemInvitation(UUID id, String secret);
}
