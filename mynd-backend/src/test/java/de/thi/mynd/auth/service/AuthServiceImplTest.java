package de.thi.mynd.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.UserNotFoundException;
import de.thi.mynd.common.service.IdentityService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;

@QuarkusTest
class AuthServiceImplTest {

  @Inject AuthServiceImpl authService;

  @InjectMock IdentityService identityService; // Wir mocken das Interface/Service

  private static final String USERNAME = "test.user";
  private static final String CLIENT_UUID = "uuid-12345";

  @BeforeEach
  void setup() {
    // Standard-Verhalten für die Client-ID
    when(identityService.getMyndClientId()).thenReturn(CLIENT_UUID);
  }

  @Test
  void testCheckUserIsBuilder_True() {
    // Setup: User hat die Rolle "builder"
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("builder", "user")));

    when(identityService.getUser(USERNAME)).thenReturn(user);

    boolean isBuilder = authService.checkUserIsBuilder(USERNAME);

    assertTrue(isBuilder);
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
  void testMakeUserABuilder_AlreadyIsBuilder() throws UserNotFoundException {
    // Setup: User ist bereits Builder
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("builder")));
    when(identityService.getUser(USERNAME)).thenReturn(user);

    authService.makeUserABuilder(USERNAME);

    // Verifikation: addRolesToUser sollte NIE aufgerufen werden
    verify(identityService, never()).addRolesToUser(anyString(), anyList());
  }

  @Test
  void testMakeUserABuilder_Success() throws UserNotFoundException {
    // Setup: User ist noch KEIN Builder
    UserRepresentation user = new UserRepresentation();
    user.setClientRoles(Map.of(CLIENT_UUID, List.of("viewer")));
    when(identityService.getUser(USERNAME)).thenReturn(user);

    authService.makeUserABuilder(USERNAME);

    // Verifikation: addRolesToUser wurde mit der korrekten Liste aufgerufen
    verify(identityService, times(1)).addRolesToUser(USERNAME, List.of("builder"));
  }

  @Test
  void testMakeUserABuilder_UserNotFound() {
    // Setup: IdentityService wirft Exception
    when(identityService.getUser(USERNAME)).thenThrow(new UserNotFoundException("Not found"));

    assertThrows(
        UserNotFoundException.class,
        () -> {
          authService.makeUserABuilder(USERNAME);
        });
  }
}
