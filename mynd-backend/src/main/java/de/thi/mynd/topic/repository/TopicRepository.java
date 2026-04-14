package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Topic;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TopicRepository extends MyndBaseRepository<Topic> {

    public List<Topic> findForCreatorPaginated(String creatorId, int page, int pageSize) {
        return find("creatorId = ?1", creatorId).page(page, pageSize)
                .list();
    }
}
