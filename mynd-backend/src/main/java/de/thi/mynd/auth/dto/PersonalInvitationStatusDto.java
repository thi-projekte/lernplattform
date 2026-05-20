package de.thi.mynd.auth.dto;

import lombok.Builder;

@Builder
public final class PersonalInvitationStatusDto {
  public int invitationsLeft;
  public long invitationsAlreadySent;
}
