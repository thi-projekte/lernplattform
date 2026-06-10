package de.thi.mynd.chat.repository;

import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.common.repository.MyndBaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class ChatMessageRepository extends MyndBaseRepository<ChatMessage> {
}
