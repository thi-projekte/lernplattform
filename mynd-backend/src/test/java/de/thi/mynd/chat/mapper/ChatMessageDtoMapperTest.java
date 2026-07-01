/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.chat.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.common.service.IdentityService;
import de.thi.mynd.progressTracking.dto.StreakDto;
import de.thi.mynd.progressTracking.service.StreakService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ChatMessageDtoMapperTest {

  @Inject ChatMessageDtoMapper chatMessageDtoMapper;

  @InjectMock IdentityService identityService;

  @InjectMock StreakService streakService;

  private ChatMessage chatMessage() {
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.id = UUID.randomUUID();
    chatMessage.creatorId = "alice";
    chatMessage.message = "hello there";
    return chatMessage;
  }

  @Test
  void mapAndEnrich_copiesScalarFieldsAndBuildsSender() {
    ChatMessage chatMessage = chatMessage();
    StreakDto streakDto = StreakDto.builder().creatorId("alice").streakCount(3).build();

    when(identityService.getFullNameByUsername("alice")).thenReturn("Alice Wonderland");
    when(streakService.getLatestPreferredStreakForUser("alice")).thenReturn(streakDto);

    ChatMessageDto dto = chatMessageDtoMapper.mapAndEnrich(chatMessage);

    assertEquals(chatMessage.id, dto.id);
    assertEquals(chatMessage.message, dto.message);
    assertNotNull(dto.sender);
    assertEquals("alice", dto.sender.creatorId);
    assertEquals("Alice Wonderland", dto.sender.creatorFullName);
    assertEquals(streakDto, dto.sender.streakToDisplay);
  }

  @Test
  void mapAndEnrich_delegatesToIdentityAndStreakServicesWithCreatorId() {
    ChatMessage chatMessage = chatMessage();

    chatMessageDtoMapper.mapAndEnrich(chatMessage);

    verify(identityService, times(1)).getFullNameByUsername("alice");
    verify(streakService, times(1)).getLatestPreferredStreakForUser("alice");
  }

  @Test
  void getEntityType_returnsChatMessage() {
    assertEquals(ChatMessage.class, chatMessageDtoMapper.getEntityType());
  }

  @Test
  void getDtoType_returnsChatMessageDto() {
    assertEquals(ChatMessageDto.class, chatMessageDtoMapper.getDtoType());
  }
}
