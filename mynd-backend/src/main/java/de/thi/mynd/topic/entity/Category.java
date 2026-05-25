package de.thi.mynd.topic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category extends BaseEntityWithId {

  @Column(nullable = false, unique = true)
  public String title;

  @Column public String color;

  @ManyToMany(mappedBy = "categories")
  @JsonIgnore
  public List<Topic> topics = new ArrayList<>();
}
