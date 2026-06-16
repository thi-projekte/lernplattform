/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "topic")
public class Topic extends BaseEntityWithId {

  @Column(nullable = false, unique = true)
  public String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String teaser;

  @Column(
      name = "title_search_vector",
      columnDefinition = "tsvector",
      insertable = false,
      updatable = false)
  public String titleSearchVector;

  @Column(
      name = "teaser_search_vector",
      columnDefinition = "tsvector",
      insertable = false,
      updatable = false)
  public String teaserSearchVector;

  @Column public int estimatedLearningDuration;

  @Column public Integer popularityScore = 0;

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(
      name = "join_topic_category",
      joinColumns = @JoinColumn(name = "topic_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"))
  public List<Category> categories = new ArrayList<>();

  @OneToMany(mappedBy = "owningTopic")
  @BatchSize(size = 20)
  public List<TopicAssociation> ownedAssociations = new ArrayList<>();

  @OneToMany(mappedBy = "foreignTopic")
  @BatchSize(size = 20)
  public List<TopicAssociation> foreignAssociations = new ArrayList<>();

  @OneToMany(mappedBy = "topic")
  public List<IndexCard> indexCards = new ArrayList<>();

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
  public List<ContentElement> contentElements = new ArrayList<>();
}
