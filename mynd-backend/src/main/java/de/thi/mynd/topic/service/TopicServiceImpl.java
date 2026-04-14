package de.thi.mynd.topic.service;

import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class TopicServiceImpl implements TopicService {

    @Inject
    SecurityIdentity identity;

    @Inject
    TopicRepository topicRepository;

    @Override
    public List<Topic> findPersonalTopicsPaginated(int page, int pageSize) {
        return topicRepository
                .findForCreatorPaginated(identity.getPrincipal().getName(), page, pageSize);
    }
}
