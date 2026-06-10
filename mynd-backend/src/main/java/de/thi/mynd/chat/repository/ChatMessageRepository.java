package de.thi.mynd.chat.repository;

import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.repository.MyndBaseRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public final class ChatMessageRepository extends MyndBaseRepository<ChatMessage> {

    public PaginationDto<ChatMessage> getChatMessagesPaginated(UUID topicId, int page, int pageSize) {
        Sort sort = Sort.descending("createdAt");
        return buildPaginationFromQuery(find("topicId = ?1", sort, topicId), page, pageSize);
    }
}
