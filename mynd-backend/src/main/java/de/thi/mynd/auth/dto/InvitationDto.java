package de.thi.mynd.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public final class InvitationDto {
    public UUID id;
    public String creatorId;
    public LocalDateTime createdAt;
}
