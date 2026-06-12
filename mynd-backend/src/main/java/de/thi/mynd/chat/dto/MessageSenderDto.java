package de.thi.mynd.chat.dto;

import de.thi.mynd.progressTracking.dto.StreakDto;
import lombok.Builder;

@Builder
public final class MessageSenderDto {

  public String creatorId;
  public String creatorFullName;
  public StreakDto streakToDisplay;
}
