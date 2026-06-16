/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.ContentElement;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public final class ContentElementRepository extends MyndBaseRepository<ContentElement> {

  public List<ContentElement> findForTopic(UUID topicId) {
    return find("topic.id = ?1", topicId).list();
  }

  public long countForTopic(UUID topicId) {
    return find("topic.id = ?1", topicId).count();
  }
}
