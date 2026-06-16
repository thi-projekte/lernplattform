/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.common.entity;

import de.thi.mynd.common.security.EntityOwnerPrePersistListener;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@EntityListeners(EntityOwnerPrePersistListener.class)
public abstract class BaseEntity {

  @Column(nullable = false, name = "creatorId")
  public String creatorId;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  public LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public LocalDateTime updatedAt;
}
