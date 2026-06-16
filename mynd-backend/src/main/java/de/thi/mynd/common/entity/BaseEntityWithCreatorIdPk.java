/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.common.entity;

import jakarta.persistence.*;

@MappedSuperclass
@AttributeOverride(
    name = "creatorId",
    column = @Column(name = "creatorId", insertable = false, updatable = false))
public abstract class BaseEntityWithCreatorIdPk extends BaseEntity {

  @EmbeddedId public CreatorIdKey id;
}
