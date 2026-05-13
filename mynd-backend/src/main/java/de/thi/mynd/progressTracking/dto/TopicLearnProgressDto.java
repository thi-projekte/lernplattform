package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public class TopicLearnProgressDto {

  public UUID topicId;
  public LearnProgressStatus status;
  public boolean completed;
  public double percentageCompleted;
  public List<UUID> completedContentElementIds;
}
