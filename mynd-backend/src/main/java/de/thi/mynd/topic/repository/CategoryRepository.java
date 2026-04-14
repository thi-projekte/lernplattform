package de.thi.mynd.topic.repository;

import de.thi.mynd.common.repository.MyndBaseRepository;
import de.thi.mynd.topic.entity.Category;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CategoryRepository extends MyndBaseRepository<Category> {

    public List<Category> findByTitleWithLimit(String title, int limit) {
        return find("title like ?1", "%" + title + "%")
                .range(0, limit)
                .list();
    }
}
