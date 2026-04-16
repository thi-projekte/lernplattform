package de.thi.mynd.topic.service;

import de.thi.mynd.topic.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> search(String query);
}
