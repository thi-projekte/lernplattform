package de.thi.mynd.chat.mapper;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.dto.MessageSenderDto;
import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.common.processor.AbstractMappingProcessor;
import de.thi.mynd.progressTracking.service.StreakService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class ChatMessageDtoMapper extends AbstractMappingProcessor<ChatMessage, ChatMessageDto> {

    @Inject
    StreakService streakService;


    @Override
    public ChatMessageDto mapAndEnrich(ChatMessage entity) {
        return ChatMessageDto.builder()
                .id(entity.id)
                .message(entity.message)
                .sender(getSender(entity.creatorId))
                .build();

    }

    @Override
    public Class<ChatMessage> getEntityType() {
        return ChatMessage.class;
    }

    @Override
    public Class<ChatMessageDto> getDtoType() {
        return ChatMessageDto.class;
    }

    private MessageSenderDto getSender(String creatorId) {

        return MessageSenderDto.builder()
                .creatorId(creatorId)
                .creatorFullName(identityService.getFullNameByUsername(creatorId))
                .streakToDisplay(streakService.getLatestPreferredStreakForUser(creatorId))
                .build();
    }
}
