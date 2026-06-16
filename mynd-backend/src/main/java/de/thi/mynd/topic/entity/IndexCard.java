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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "index_card")
public class IndexCard extends BaseEntityWithId {

  @ManyToOne
  @JoinColumn(name = "topicId", referencedColumnName = "id", nullable = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Topic topic;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String question;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String answer;
}
