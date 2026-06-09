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
