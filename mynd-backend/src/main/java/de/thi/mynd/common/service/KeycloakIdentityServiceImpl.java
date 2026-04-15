package de.thi.mynd.common.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@ApplicationScoped
public class KeycloakIdentityServiceImpl implements IdentityService{

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @Override
    public String getFullNameByUsername(String username) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(username, true);
        if (users.isEmpty()) return username;
        UserRepresentation user = users.get(0);
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
