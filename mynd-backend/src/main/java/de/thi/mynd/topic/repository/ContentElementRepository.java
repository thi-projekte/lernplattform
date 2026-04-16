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
}
