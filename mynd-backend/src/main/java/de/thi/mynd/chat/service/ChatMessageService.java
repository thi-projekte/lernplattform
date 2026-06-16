/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.chat.service;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.request.ChatMessageRequest;
import de.thi.mynd.common.dto.PaginationDto;
import java.util.UUID;

public interface ChatMessageService {

  ChatMessageDto sendMessageToTopic(UUID topicId, ChatMessageRequest request);

  PaginationDto<ChatMessageDto> getMessages(UUID topicId, int page, int pageSize);
}
