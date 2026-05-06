package de.thi.mynd.common.service;

import de.thi.mynd.common.exception.UserNotFoundException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@ApplicationScoped
public final class KeycloakIdentityServiceImpl implements IdentityService {

  @Inject Keycloak keycloak;

  @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
  String realm;

  @Override
  public String getMyndClientId() {
    return keycloak.realm(realm).clients().findByClientId("mynd").get(0).getId();
  }

  @Override
  public String getFullNameByUsername(String username) {
    Log.debugf("Fetching user from keycloak for username: %s", username);
    List<UserRepresentation> users = keycloak.realm(realm).users().search(username, true);
    if (users.isEmpty()) return username;
    UserRepresentation user = users.get(0);
    if (user.getFirstName() == null && user.getLastName() == null) {
      return username;
    }
    return ((user.getFirstName() == null ? "" : user.getFirstName())
            + " "
            + (user.getLastName() == null ? "" : user.getLastName()))
        .trim();
  }

  @Override
  public UserRepresentation getUser(String username) {
    try {
      return keycloak.realm(realm).users().search(username).getFirst();
    } catch (NoSuchElementException e) {
      throw new UserNotFoundException("This user does not exist");
    }
  }

  @Override
  public List<RoleRepresentation> getMyndRoles(String username) throws UserNotFoundException {
    UserRepresentation user = getUser(username);
    String clientUuid = getMyndClientId();

    List<RoleRepresentation> clientRoles =
        keycloak.realm(realm).users().get(user.getId()).roles().clientLevel(clientUuid).listAll();

    return clientRoles;
  }

  @Override
  public void addRolesToUser(String username, List<String> myndRoles) throws UserNotFoundException {
    UserRepresentation user = getUser(username);
    String clientUuid = getMyndClientId();
    List<RoleRepresentation> existingRole = getMyndRoles(username);

    List<RoleRepresentation> reps = new ArrayList<>();
    for (String role : myndRoles) {
      RoleRepresentation roleRepresentation =
          keycloak.realm(realm).clients().get(clientUuid).roles().get(role).toRepresentation();
      if (!existingRole.contains(roleRepresentation)) {
        reps.add(roleRepresentation);
      }
    }
    keycloak.realm(realm).users().get(user.getId()).roles().clientLevel(clientUuid).add(reps);
  }
}
