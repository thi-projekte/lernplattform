/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.chat.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.chat.entity.ChatMessage;
import de.thi.mynd.common.dto.PaginationDto;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link ChatMessageRepository} against a real Postgres instance (Quarkus dev services).
 * {@code topicId} is a plain UUID column without an enforced relationship, so no real Topic needs
 * to be persisted for these tests.
 */
@QuarkusTest
class ChatMessageRepositoryTest {

  @Inject ChatMessageRepository chatMessageRepository;

  private ChatMessage newChatMessage(UUID topicId, String message) {
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.topicId = topicId;
    chatMessage.message = message;
    chatMessage.creatorId = "tester";
    return chatMessage;
  }

  @Test
  @TestTransaction
  void getChatMessagesPaginated_ordersNewestFirst() throws InterruptedException {
    UUID topicId = UUID.randomUUID();
    ChatMessage first = newChatMessage(topicId, "first message");
    chatMessageRepository.persistAndFlush(first);
    Thread.sleep(5);
    ChatMessage second = newChatMessage(topicId, "second message");
    chatMessageRepository.persistAndFlush(second);
    Thread.sleep(5);
    ChatMessage third = newChatMessage(topicId, "third message");
    chatMessageRepository.persistAndFlush(third);

    PaginationDto<ChatMessage> result =
        chatMessageRepository.getChatMessagesPaginated(topicId, 0, 10);

    assertEquals(3, result.results.size());
    assertEquals(third.id, result.results.get(0).id);
    assertEquals(second.id, result.results.get(1).id);
    assertEquals(first.id, result.results.get(2).id);
  }

  @Test
  @TestTransaction
  void getChatMessagesPaginated_respectsPageSizeAndTotalPages() {
    UUID topicId = UUID.randomUUID();
    for (int i = 0; i < 5; i++) {
      chatMessageRepository.persistAndFlush(newChatMessage(topicId, "message-" + i));
    }

    PaginationDto<ChatMessage> result =
        chatMessageRepository.getChatMessagesPaginated(topicId, 0, 2);

    assertEquals(2, result.results.size());
    assertEquals(3, result.totalPages);
  }

  @Test
  @TestTransaction
  void getChatMessagesPaginated_excludesMessagesForOtherTopics() {
    UUID topicId = UUID.randomUUID();
    UUID otherTopicId = UUID.randomUUID();
    chatMessageRepository.persistAndFlush(newChatMessage(topicId, "in scope"));
    chatMessageRepository.persistAndFlush(newChatMessage(otherTopicId, "out of scope"));

    PaginationDto<ChatMessage> result =
        chatMessageRepository.getChatMessagesPaginated(topicId, 0, 10);

    List<ChatMessage> messages = result.results;
    assertEquals(1, messages.size());
    assertEquals("in scope", messages.get(0).message);
  }
}
