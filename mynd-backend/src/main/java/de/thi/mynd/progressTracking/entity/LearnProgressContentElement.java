/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.entity;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "learn_progress_content_element")
@AttributeOverride(
    name = "creatorId",
    column = @Column(name = "creatorId", insertable = false, updatable = false))
public class LearnProgressContentElement extends BaseEntity {

  @EmbeddedId public LearnProgressContentElementId id;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(
        name = "creatorId",
        referencedColumnName = "creatorId",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "topicId",
        referencedColumnName = "topicId",
        insertable = false,
        updatable = false)
  })
  public LearnProgressTopic progressTopic;

  @Column(nullable = false)
  public boolean completed;
}
