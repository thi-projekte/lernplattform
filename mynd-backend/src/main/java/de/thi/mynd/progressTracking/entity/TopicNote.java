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
@Table(name = "topic_note")
@AttributeOverride(
    name = "creatorId",
    column = @Column(name = "creatorId", insertable = false, updatable = false))
public class TopicNote extends BaseEntity {

  @EmbeddedId public TopicNoteId id;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String content;
}
