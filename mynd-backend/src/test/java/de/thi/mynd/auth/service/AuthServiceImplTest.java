package de.thi.mynd.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.auth.RegisterRole;
import de.thi.mynd.auth.dto.RegisterUserRequestDto;
import de.thi.mynd.auth.exception.UserAlreadyExistsException;
import de.thi.mynd.common.service.IdentityService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;

import java.util.List;

@QuarkusTest
class AuthServiceImplTest {

  @Inject AuthService authService;

  @InjectMock IdentityService identityService;

  @Test
  void testCheckUsernameExists_ReturnsTrue() {
    when(identityService.userExists("tester")).thenReturn(true);

    boolean exists = authService.checkUsernameExists("tester");

    assertTrue(exists);
    verify(identityService).userExists("tester");
  }

  @Test
  void testRegisterUser_Success() throws UserAlreadyExistsException {
    // Arrange
    RegisterUserRequestDto dto = new RegisterUserRequestDto();
    dto.username = "newuser";
    dto.password = "password123";
    dto.email = "test@example.com";
    dto.firstName = "John";
    dto.lastName = "Doe";
    dto.role = RegisterRole.Builder;

    // Logic check: service checks if user exists. We return FALSE so it continues.
    when(identityService.userExists("newuser")).thenReturn(false);

    // Act
    authService.registerUser(dto);

    // Assert
    ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<UserRepresentation> captor2 = ArgumentCaptor.forClass(UserRepresentation.class);
    verify(identityService).createUser(captor2.capture(), captor.capture());

    UserRepresentation capturedUser = captor2.getValue();
    assertEquals("test@example.com", capturedUser.getEmail());
    List<String> roles = captor.getValue();
    assertTrue(roles.contains("builder"));
  }

  @Test
  void testRegisterUser_ThrowsExceptionWhenExists() {
    RegisterUserRequestDto dto = new RegisterUserRequestDto();
    dto.username = "existingUser";

    // Logic check: return TRUE to trigger the exception
    when(identityService.userExists("existingUser")).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class,
        () -> {
          authService.registerUser(dto);
        });

    verify(identityService, never()).createUser(any(), any());
  }
}
