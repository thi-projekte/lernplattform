/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.chat.dto;

import de.thi.mynd.progressTracking.dto.StreakDto;
import lombok.Builder;

@Builder
public final class MessageSenderDto {

  public String creatorId;
  public String creatorFullName;
  public StreakDto streakToDisplay;
}
