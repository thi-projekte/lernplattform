/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.service;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import java.util.List;
import java.util.UUID;

public interface TopicAssociationService {

  List<TopicAssociation> findOrCreateOwningTopicAssociationsOwnedByUserNoFlush(
      Topic topic, List<AssociatedEntityRequest> associatedTopics, String username);

  TopicAssociation createAssociation(UUID owningTopic, UUID foreignTopic);

  void deleteAssociation(UUID associationId);
}
