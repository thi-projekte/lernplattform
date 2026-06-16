/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.exception.UserNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@QuarkusTest
class KeycloakIdentityServiceImplTest {

  @Inject KeycloakIdentityServiceImpl identityService;

  @InjectMock Keycloak keycloak;

  RealmResource realmResource = mock(RealmResource.class);
  UsersResource usersResource = mock(UsersResource.class);
  ClientsResource clientsResource = mock(ClientsResource.class);
  ClientResource clientResource = mock(ClientResource.class);
  RolesResource rolesResource = mock(RolesResource.class);
  RoleResource roleResource = mock(RoleResource.class);
  UserResource userResource = mock(UserResource.class);
  RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
  RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);

  @BeforeEach
  void setup() {
    identityService.realm = "test-realm";
    // Grundlegendes Setup der Kette: keycloak.realm(realm)...
    when(keycloak.realm(anyString())).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
    when(realmResource.clients()).thenReturn(clientsResource);

    ClientRepresentation clientRep = new ClientRepresentation();
    clientRep.setId("mock-uuid-12345");
    when(clientsResource.findByClientId("mynd")).thenReturn(List.of(clientRep));
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

  @Test
  void testGetUser_Success() {
    UserRepresentation userRep = new UserRepresentation();
    userRep.setUsername("testuser");

    when(usersResource.search("testuser")).thenReturn(List.of(userRep));

    UserRepresentation result = identityService.getUser("testuser");

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void testGetUser_NotFound() {
    // Simuliere eine leere Liste -> getFirst() wirft NoSuchElementException
    when(usersResource.search("unknown")).thenReturn(Collections.emptyList());

    assertThrows(
        UserNotFoundException.class,
        () -> {
          identityService.getUser("unknown");
        });
  }

  @Test
  void testAddRolesToUser_Success() throws UserNotFoundException {
    // 1. Mock für getUser() innerhalb der Methode
    UserRepresentation userRep = new UserRepresentation();
    userRep.setId("user-123");
    userRep.setUsername("testuser");
    when(usersResource.search("testuser")).thenReturn(List.of(userRep));

    // 2. Mock für Rollen-Abruf
    String roleName = "admin";
    String clientUuid = "client-uuid-007";
    // Wir nehmen an, getMyndClientId() gibt "client-uuid-007" zurück
    // Falls getMyndClientId() ein lokaler Methodenaufruf ist, muss der Service ggf. teilweise
    // gemockt werden

    when(clientsResource.get(anyString())).thenReturn(clientResource);
    when(clientResource.roles()).thenReturn(rolesResource);
    when(rolesResource.get(roleName)).thenReturn(roleResource);

    RoleRepresentation roleRep = new RoleRepresentation();
    roleRep.setName(roleName);
    when(roleResource.toRepresentation()).thenReturn(roleRep);

    // 3. Mock für das Hinzufügen der Rolle
    when(usersResource.get("user-123")).thenReturn(userResource);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(roleMappingResource.clientLevel(anyString())).thenReturn(roleScopeResource);

    // Ausführung
    identityService.addRolesToUser("testuser", List.of("admin"));

    // Verifikation
    verify(roleScopeResource, times(1)).add(anyList());
  }
}
