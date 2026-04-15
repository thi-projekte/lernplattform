package de.thi.mynd.common.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@ApplicationScoped
public final class KeycloakProducer {

    @ConfigProperty(name = "quarkus.keycloak.admin-client.server-url")
    String serverUrl;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.grant-type")
    String grantType;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-secret")
    String clientSecret;

    @Produces
    @ApplicationScoped
    public Keycloak createKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(grantType)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
