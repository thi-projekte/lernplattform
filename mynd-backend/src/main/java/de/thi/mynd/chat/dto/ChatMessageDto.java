/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.chat.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public final class ChatMessageDto {

  public UUID id;
  public String message;
  public MessageSenderDto sender;
}
