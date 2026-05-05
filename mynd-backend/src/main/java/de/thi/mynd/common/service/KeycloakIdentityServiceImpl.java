package de.thi.mynd.common.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

@ApplicationScoped
public final class KeycloakIdentityServiceImpl implements IdentityService {

  @Inject Keycloak keycloak;

  @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
  String realm;

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
  public boolean userExists(String username) {
    return !keycloak.realm(realm).users().search(username).isEmpty();
  }

  @Override
  public void createUser(UserRepresentation userRepresentation) {
    Response response = keycloak.realm(realm).users().create(userRepresentation);
    if (response.getStatus() == 201) {
      Log.infof("Successfully registered new user: %s", userRepresentation.getUsername());
    } else {
      Log.error("Error while creating new user");
    }
  }
}
