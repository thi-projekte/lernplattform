package de.thi.mynd.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

@QuarkusTest
class KeycloakIdentityServiceImplTest {

  @Inject KeycloakIdentityServiceImpl identityService;

  @InjectMock Keycloak keycloak;

  // We need to mock the nested resources in the Keycloak fluent API
  RealmResource realmResource = mock(RealmResource.class);
  UsersResource usersResource = mock(UsersResource.class);

  private static final String REALM = "test-realm";

  @BeforeEach
  void setup() {
    identityService.realm = REALM;
    // Set up the fluent chain: keycloak.realm(...).users()
    when(keycloak.realm(REALM)).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
  }

  @Test
  void testGetFullNameByUsername_Success() {
    // Arrange
    String username = "jdoe";
    UserRepresentation user = new UserRepresentation();
    user.setFirstName("John");
    user.setLastName("Doe");

    when(usersResource.search(username, true)).thenReturn(List.of(user));

    // Act
    String fullName = identityService.getFullNameByUsername(username);

    // Assert
    assertEquals("John Doe", fullName);
  }

  @Test
  void testGetFullNameByUsername_UserNotFound() {
    // Arrange
    String username = "unknown";
    when(usersResource.search(username, true)).thenReturn(Collections.emptyList());

    // Act
    String fullName = identityService.getFullNameByUsername(username);

    // Assert
    assertEquals("unknown", fullName);
  }

  @Test
  void testGetFullNameByUsername_NoNamesSet() {
    // Arrange
    String username = "no-name-user";
    UserRepresentation user = new UserRepresentation();
    user.setFirstName(null);
    user.setLastName(null);

    when(usersResource.search(username, true)).thenReturn(List.of(user));

    // Act
    String fullName = identityService.getFullNameByUsername(username);

    // Assert
    assertEquals("no-name-user", fullName);
  }

  @Test
  void testGetFullNameByUsername_OnlyFirstName() {
    // Arrange
    String username = "just-john";
    UserRepresentation user = new UserRepresentation();
    user.setFirstName("John");
    user.setLastName(null);

    when(usersResource.search(username, true)).thenReturn(List.of(user));

    // Act
    String fullName = identityService.getFullNameByUsername(username);

    // Assert
    assertEquals("John", fullName);
  }
}
