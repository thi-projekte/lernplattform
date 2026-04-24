package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import java.util.List;

public interface TopicAssociationService {

  List<TopicAssociation> findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
      Topic topic, List<AssociatedEntityRequest> associatedTopics, String username);
}
