package de.thi.mynd.auth.dto;

import jakarta.validation.constraints.NotBlank;

public final class RedeemInvitationDto {

  @NotBlank public String secret;
}
