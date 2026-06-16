/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.mapper;

import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.entity.LearnProgressContentElement;
import de.thi.mynd.progressTracking.entity.LearnProgressStatus;
import de.thi.mynd.progressTracking.entity.LearnProgressTopic;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicLearnProgressDtoMapper
    extends AbstractMappingProcessor<LearnProgressTopic, TopicLearnProgressDto> {
  @Override
  public TopicLearnProgressDto mapAndEnrich(LearnProgressTopic entity) {

    List<LearnProgressContentElement> elements = entity.contentElements;

    List<UUID> completedIds =
        elements.stream().filter(e -> e.completed).map(e -> e.id.contentElementId).toList();

    boolean completed =
        entity.status == LearnProgressStatus.COMPLETED_MANUALLY
            || entity.status == LearnProgressStatus.ALL_CONTENT_ELEMENTS_COMPLETED;

    double percentageCompleted =
        (double) completedIds.size() / (double) entity.contentElementsToComplete;

    return TopicLearnProgressDto.builder()
        .topicId(entity.id.topicId)
        .status(entity.status)
        .completed(completed)
        .completedContentElementIds(completedIds)
        .percentageCompleted(percentageCompleted)
        .build();
  }

  @Override
  public Class<LearnProgressTopic> getEntityType() {
    return LearnProgressTopic.class;
  }

  @Override
  public Class<TopicLearnProgressDto> getDtoType() {
    return TopicLearnProgressDto.class;
  }
}
