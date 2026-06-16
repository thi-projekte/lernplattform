/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.request.IndexCardRequest;
import java.util.List;
import java.util.UUID;

public interface IndexCardService {

  List<IndexCardDto> getIndexCardsForTopic(UUID topicId);

  IndexCardDto createIndexCard(IndexCardRequest request);

  void deleteIndexCard(UUID cardId);

  void updateTopicAssociation(Topic topic, List<AssociatedEntityRequest> elements);
}
