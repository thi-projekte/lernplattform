package de.thi.mynd.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public final class InvitationDto {
  public UUID id;
  public String creatorId;
  public LocalDateTime createdAt;
}
