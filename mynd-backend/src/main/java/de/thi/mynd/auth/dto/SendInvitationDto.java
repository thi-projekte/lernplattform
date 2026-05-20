package de.thi.mynd.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class SendInvitationDto {

  @NotBlank @Email public String email;
}
