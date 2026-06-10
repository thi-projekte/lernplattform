package de.thi.mynd.topic.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public final class IndexCardDto {
  public UUID id;
  public String question;
  public String answer;
}
