/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.common.service;

import de.thi.mynd.common.exception.UserNotFoundException;
import io.quarkus.cache.CacheResult;
import java.util.List;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public interface IdentityService {

  @CacheResult(cacheName = "client-id")
  String getMyndClientId();

  @CacheResult(cacheName = "external-user")
  String getFullNameByUsername(String username);

  @CacheResult(cacheName = "get-user")
  UserRepresentation getUser(String username) throws UserNotFoundException;

  List<RoleRepresentation> getMyndRoles(String username) throws UserNotFoundException;

  void addRolesToUser(String username, List<String> myndRoles) throws UserNotFoundException;
}
