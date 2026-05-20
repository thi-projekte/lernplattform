package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.common.dto.PaginationDto;

import java.util.UUID;

public interface InvitationService {

    InvitationDto getInvitation(UUID invitationId);

    PaginationDto<InvitationDto> getSentInvitations(int page, int pageSize);

    PersonalInvitationStatusDto getPersonalInvitationStatus();

    void redeemInvitation(UUID id, String secret);

    void sendInvitation(String email);
}
