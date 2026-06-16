/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.auth.entity;

import de.thi.mynd.common.entity.BaseEntityWithCreatorIdPk;
import jakarta.persistence.*;

@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseEntityWithCreatorIdPk {

  @Column public String profilePictureKey;

  @Column(nullable = false)
  public int invitationsLeft;
}
