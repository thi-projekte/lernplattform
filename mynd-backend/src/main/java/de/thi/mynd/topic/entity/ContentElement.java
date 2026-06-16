/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "content_element")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ContentElement extends BaseEntityWithId {

  @Column(nullable = false)
  public String title;

  @Column(name = "type", insertable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  public ContentType type;

  @Column(length = 64)
  public String icon = "";

  @Column(name = "rank", nullable = true)
  public Integer rank;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "topic_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Topic topic;
}
