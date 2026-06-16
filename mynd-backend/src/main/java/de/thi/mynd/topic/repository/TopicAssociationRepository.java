/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.entity.TopicAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class TopicAssociationRepository extends MyndBaseRepository<TopicAssociation> {

  public List<TopicAssociation> findOwningAssociationsByIdsAndUsername(
      UUID topicId, List<UUID> ids, String username) {
    return find(
            "owningTopic.id = ?1 AND creatorId = ?2 AND foreignTopic.id IN ?3",
            topicId,
            username,
            ids)
        .list();
  }

  public boolean associationExists(Topic owner, Topic foreign) {
    return find(
                "(owningTopic.id = ?1 AND foreignTopic.id = ?2) OR (owningTopic.id = ?2 AND foreignTopic.id = ?1)",
                owner.id,
                foreign.id)
            .count()
        > 0;
  }
}
