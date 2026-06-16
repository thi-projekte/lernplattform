/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.chat.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import de.thi.mynd.chat.dto.ChatMessageDto;
import de.thi.mynd.chat.service.ChatMessageService;
import de.thi.mynd.common.dto.PaginationDto;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ChatMessageResourceTest {

  @InjectMock ChatMessageService chatMessageService;

  // ==========================================
  // Security / Authorization Tests
  // ==========================================

  @Test
  void testGetChatMessages_Unauthorized() {
    UUID topicId = UUID.randomUUID();

    // No @TestSecurity annotation means the request acts as an anonymous/unauthenticated user
    given()
        .pathParam("topicId", topicId)
        .queryParam("page", 0)
        .queryParam("pageSize", 10)
        .when()
        .get("/chats/{topicId}")
        .then()
        .statusCode(401); // Expect Unauthorized because of @RolesAllowed

    verifyNoInteractions(chatMessageService);
  }

  @Test
  @TestSecurity(user = "wrongUser", roles = "someOtherRole")
  void testGetChatMessages_ForbiddenRole() {
    UUID topicId = UUID.randomUUID();

    // Logged in with a role, but not the correct "authorizedUser" role
    given()
        .pathParam("topicId", topicId)
        .queryParam("page", 0)
        .queryParam("pageSize", 10)
        .when()
        .get("/chats/{topicId}")
        .then()
        .statusCode(403); // Expect Forbidden

    verifyNoInteractions(chatMessageService);
  }

  // ==========================================
  // Functional Endpoint Tests
  // ==========================================

  @Test
  @TestSecurity(user = "alice", roles = "authorizedUser")
  void testGetChatMessages_Success() {
    UUID topicId = UUID.randomUUID();
    int page = 0;
    int pageSize = 5;

    // Build mock DTO payload
    ChatMessageDto msg = ChatMessageDto.builder().build();
    // Set fields if needed, e.g., msg.setMessage("Hello");

    PaginationDto<ChatMessageDto> mockPaginationResult =
        PaginationDto.<ChatMessageDto>builder().results(List.of(msg)).totalPages(2).build();

    // Stub service response
    when(chatMessageService.getMessages(topicId, page, pageSize)).thenReturn(mockPaginationResult);

    // Execute REST request and verify JSON body structure
    given()
        .pathParam("topicId", topicId)
        .queryParam("page", page)
        .queryParam("pageSize", pageSize)
        .contentType(ContentType.JSON)
        .when()
        .get("/chats/{topicId}")
        .then()
        .statusCode(200)
        .body("totalPages", equalTo(2))
        .body("results", hasSize(1));

    // Verify the JAX-RS path/query parameters were unmarshalled and sent to the service layer
    // properly
    verify(chatMessageService, times(1)).getMessages(topicId, page, pageSize);
  }
}
