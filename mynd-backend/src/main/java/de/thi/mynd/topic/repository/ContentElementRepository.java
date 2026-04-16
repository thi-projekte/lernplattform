package de.thi.mynd.topic.repository;

import de.thi.mynd.topic.entity.ContentElement;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContentElementRepository implements PanacheRepository<ContentElement> {
}
