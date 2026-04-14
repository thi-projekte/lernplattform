package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category extends BaseEntity {

  @Column(nullable = false)
  public String title;

  @Column public String color;

  @ManyToMany(mappedBy = "categories")
  public List<Topic> topics = new ArrayList<>();
}
