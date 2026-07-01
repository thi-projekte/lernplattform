/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.thi.mynd.topic.entity.Category;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EntityOwnerPrePersistListenerTest {

  @Inject EntityOwnerPrePersistListener listener;

  @InjectMock SecurityIdentity securityIdentity;

  @Test
  void setOwner_nonBaseEntitySubject_doesNothing() {
    assertDoesNotThrow(() -> listener.setOwner(new Object()));
  }

  @Test
  void setOwner_creatorIdAlreadySet_leavesItUnchanged() {
    Category category = new Category();
    category.creatorId = "existing-owner";

    listener.setOwner(category);

    assertEquals("existing-owner", category.creatorId);
  }

  @Test
  void setOwner_noCreatorIdAndAuthenticated_setsFromSecurityIdentity() {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn("alice");
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(securityIdentity.getPrincipal()).thenReturn(principal);

    Category category = new Category();

    listener.setOwner(category);

    assertEquals("alice", category.creatorId);
  }

  @Test
  void setOwner_noCreatorIdAndAnonymous_fallsBackToAdmin() {
    when(securityIdentity.isAnonymous()).thenReturn(true);

    Category category = new Category();

    listener.setOwner(category);

    assertEquals("admin", category.creatorId);
  }
}
