/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public final class InvitationDto {
  public UUID id;
  public String creatorId;
  public String mailSentTo;
  public LocalDateTime createdAt;
  public boolean accepted;
}
