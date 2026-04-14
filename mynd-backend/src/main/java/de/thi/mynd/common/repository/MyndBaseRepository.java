package de.thi.mynd.common.repository;

import de.thi.mynd.common.entity.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;

public abstract class MyndBaseRepository<T extends BaseEntity> implements PanacheRepository<T> {

    public List<T> findAllWithLimit(int limit) {
        return findAll().range(0, limit).list();
    }
}
