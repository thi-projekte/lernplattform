/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.dto;

import de.thi.mynd.topic.entity.Category;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public final class ListTopicDto {

  public UUID id;

  public String title;

  public List<Category> categories;

  public LocalDateTime updatedAt;

  public String creatorId;

  public String creatorFullName;
}
