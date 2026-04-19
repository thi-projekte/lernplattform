package de.thi.mynd.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
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

  RealmResource realmResource = mock(RealmResource.class);
  UsersResource usersResource = mock(UsersResource.class);

  @BeforeEach
  void setup() {
    // Ensure the service uses a specific string
    identityService.realm = "test-realm";

    // Use anyString() to avoid mismatch during the proxy's execution
    when(keycloak.realm(anyString())).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
  }

  @Test
  void testGetFullNameByUsername_Success() {
    String username = "jdoe";
    UserRepresentation user = new UserRepresentation();
    user.setFirstName("John");
    user.setLastName("Doe");

    when(usersResource.search(username, true)).thenReturn(List.of(user));

    String fullName = identityService.getFullNameByUsername(username);
    assertEquals("John Doe", fullName);
  }
}
