/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.thi.mynd.common.entity.BaseEntityWithId;
import de.thi.mynd.common.entity.LtreeType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "category")
public class Category extends BaseEntityWithId {

  @Column(nullable = false, unique = true)
  public String title;

  @Column public String color;

  @Column(columnDefinition = "ltree")
  @Type(LtreeType.class)
  public String path;

  @ManyToMany(mappedBy = "categories")
  @JsonIgnore
  public List<Topic> topics = new ArrayList<>();
}
