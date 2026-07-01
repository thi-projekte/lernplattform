/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.exception.UserNotFoundException;
import de.thi.mynd.common.service.IdentityService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@QuarkusTest
class AuthServiceImplTest {

  @Inject AuthServiceImpl authService;

  @InjectMock IdentityService identityService; // Wir mocken das Interface/Service

  @InjectMock UserProfileService userProfileService;

  private static final String USERNAME = "test.user";
  private static final String CLIENT_UUID = "uuid-12345";

  @BeforeEach
  void setup() {
    // Standard-Verhalten für die Client-ID
    when(identityService.getMyndClientId()).thenReturn(CLIENT_UUID);
  }

  @Test
  void testCheckUserIsBuilder_False() {
    // Setup: User hat andere Rollen, aber kein "builder"
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("viewer")));

    when(identityService.getUser(USERNAME)).thenReturn(user);

    boolean isBuilder = authService.checkUserIsBuilder(USERNAME);

    assertFalse(isBuilder);
  }

  @Test
  void testCheckUserIsBuilder_True() {
    // Setup: User has the "builder" role among their Mynd roles
    RoleRepresentation builderRole = new RoleRepresentation();
    builderRole.setName("builder");

    when(identityService.getMyndRoles(USERNAME)).thenReturn(List.of(builderRole));

    boolean isBuilder = authService.checkUserIsBuilder(USERNAME);

    assertTrue(isBuilder);
  }

  @Test
  void testMakeUserABuilder_AlreadyIsBuilder() throws UserNotFoundException {
    // Setup: User ist bereits Builder
    UserProfile up = new UserProfile();
    up.invitationsLeft = 0;
    up.creatorId = USERNAME;

    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("builder")));
    when(identityService.getUser(USERNAME)).thenReturn(user);
    when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.of(up));

    authService.makeUserABuilder(USERNAME);

    verify(identityService, times(1)).addRolesToUser(anyString(), anyList());
  }

  @Test
  void testMakeUserABuilder_Success() throws UserNotFoundException {
    // Setup: User ist noch KEIN Builder
    UserProfile up = new UserProfile();
    up.invitationsLeft = 0;
    up.creatorId = USERNAME;
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("viewer")));
    when(identityService.getUser(USERNAME)).thenReturn(user);
    when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.of(up));

    authService.makeUserABuilder(USERNAME);

    // Verifikation: addRolesToUser wurde mit der korrekten Liste aufgerufen
    verify(identityService, times(1)).addRolesToUser(USERNAME, List.of("builder"));
  }

  @Test
  void testMakeUserALearner_Success() throws UserNotFoundException {
    // Setup: User ist noch KEIN Builder
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("viewer")));
    UserProfile up = new UserProfile();
    up.invitationsLeft = 0;
    up.creatorId = USERNAME;
    when(identityService.getUser(USERNAME)).thenReturn(user);
    when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.of(up));

    authService.makeUserALearner(USERNAME);

    // Verifikation: addRolesToUser wurde mit der korrekten Liste aufgerufen
    verify(identityService, times(1)).addRolesToUser(USERNAME, List.of("learner"));
  }

  @Test
  void testMakeUserALearner_NoExistingProfile_CreatesPersonalProfile()
      throws UserNotFoundException {
    // Setup: kein bestehendes UserProfile vorhanden -> orElseGet Fallback wird ausgelöst
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("viewer")));
    UserProfile newlyCreated = new UserProfile();
    newlyCreated.invitationsLeft = 0;
    newlyCreated.creatorId = USERNAME;

    when(identityService.getUser(USERNAME)).thenReturn(user);
    when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.empty());
    when(userProfileService.createPersonalUserProfile()).thenReturn(newlyCreated);

    authService.makeUserALearner(USERNAME);

    verify(userProfileService, times(1)).createPersonalUserProfile();
    verify(identityService, times(1)).addRolesToUser(USERNAME, List.of("learner"));
  }
}
