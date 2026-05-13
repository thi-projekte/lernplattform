package de.thi.mynd.progressTracking.dto;

import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public class TopicLearnProgressDto {

    public UUID topicId;
    public LearnProgressStatus status;
    public boolean completed;
    public int percentageCompleted;
    public List<UUID> completedContentElementIds;
}
