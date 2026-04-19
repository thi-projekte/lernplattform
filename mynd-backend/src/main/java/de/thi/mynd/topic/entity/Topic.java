package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topic")
public class Topic extends BaseEntity {

  @Column(nullable = false)
  public String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String teaser;

  @Column public int estimatedLearningDuration;

  @Column(nullable = false)
  public String creatorId;

  @ManyToMany
  @JoinTable(
      name = "join_topic_category",
      joinColumns = @JoinColumn(name = "topic_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"))
  public List<Category> categories = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "topic_relations",
      joinColumns = @JoinColumn(name = "topic_id"),
      inverseJoinColumns = @JoinColumn(name = "related_topic_id"))
  public List<Topic> relatedTopics = new ArrayList<>();

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<ContentElement> contentElements = new ArrayList<>();
}
