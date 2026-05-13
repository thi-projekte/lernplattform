package de.thi.mynd.common.service;

import de.thi.mynd.common.exception.UserNotFoundException;
import io.quarkus.cache.CacheResult;
import java.util.List;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public interface IdentityService {

  /**
   * Resolves the client ID for the mynd application. Result is cached under {@code "client-id"}.
   *
   * @return the mynd client ID
   */
  @CacheResult(cacheName = "client-id")
  String getMyndClientId();

  /**
   * Resolves the full display name for the given username. Result is cached under {@code
   * "external-user"}.
   *
   * @param username the Keycloak username to look up
   * @return the user's full name
   */
  @CacheResult(cacheName = "external-user")
  String getFullNameByUsername(String username);

  /**
   * Retrieves the full Keycloak {@link UserRepresentation} for the given username. Result is cached
   * under {@code "get-user"}.
   *
   * @param username the Keycloak username to look up
   * @return the corresponding {@link UserRepresentation}
   * @throws UserNotFoundException if no user exists with the given username
   */
  @CacheResult(cacheName = "get-user")
  UserRepresentation getUser(String username) throws UserNotFoundException;

  /**
   * Retrieves all mynd-specific Keycloak client roles assigned to the given user.
   *
   * @param username the Keycloak username to look up
   * @return a list of the user's assigned {@link RoleRepresentation}s; never {@code null}
   * @throws UserNotFoundException if no user exists with the given username
   */
  List<RoleRepresentation> getMyndRoles(String username) throws UserNotFoundException;

  /**
   * Assigns the given mynd client roles to the specified user in Keycloak. Roles are matched by
   * name against the available mynd client roles.
   *
   * @param username the Keycloak username to assign roles to
   * @param myndRoles the list of role names to assign
   * @throws UserNotFoundException if no user exists with the given username
   */
  void addRolesToUser(String username, List<String> myndRoles) throws UserNotFoundException;
}
