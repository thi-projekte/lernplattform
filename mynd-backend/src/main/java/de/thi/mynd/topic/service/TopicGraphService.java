/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.service;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.topic.dto.graph.GraphTopicDto;
import java.util.List;
import java.util.UUID;

public interface TopicGraphService {

  List<GraphTopicDto> getLearnGraph();

  List<GraphTopicDto> getNeighborsOfTopic(UUID topicId) throws EntityInstanceNotFoundException;

  List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(int n, String creatorId);

  List<GraphTopicDto> getNMostPopularTopicsInGraphAndTheirDirectNeighbors(
      int n, List<UUID> categoryFilter, String creatorId);

  List<GraphTopicDto> getOwnedNeighborsOfTopic(UUID topicId) throws EntityInstanceNotFoundException;

  List<GraphTopicDto> searchTopicNodes(String search, int limit);
}
