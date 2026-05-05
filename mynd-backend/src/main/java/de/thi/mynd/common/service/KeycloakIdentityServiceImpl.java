package de.thi.mynd.common.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
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
  public void createUser(UserRepresentation userRepresentation, List<String> myndRoles) {
    RealmResource realmResource = keycloak.realm(realm);
    UsersResource usersRessource = realmResource.users();

    // TODO: Handle case with already existing email
    // TODO: Do better error handling

    Response response = usersRessource.create(userRepresentation);;
    String userId = CreatedResponseUtil.getCreatedId(response);
    String clientUuid = keycloak.realm(realm).clients().findByClientId("mynd").get(0).getId();

    List<RoleRepresentation> reps = new ArrayList<>();
    for (String role : myndRoles) {
      RoleRepresentation roleRepresentation = keycloak.realm(realm).clients().get(clientUuid).roles().get(role).toRepresentation();
      reps.add(roleRepresentation);
    }

      keycloak.realm(realm).users().get(userId).roles().clientLevel(clientUuid).add(reps);
  }
}
