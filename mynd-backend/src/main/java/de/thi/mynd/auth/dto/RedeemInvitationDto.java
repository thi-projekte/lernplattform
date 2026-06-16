/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.auth.dto;

import jakarta.validation.constraints.NotBlank;

public final class RedeemInvitationDto {

  @NotBlank public String secret;
}
