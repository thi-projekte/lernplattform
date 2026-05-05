package de.thi.mynd.common.service;

import io.quarkus.cache.CacheResult;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IdentityService {

  @CacheResult(cacheName = "external-user")
  String getFullNameByUsername(String username);

  @CacheResult(cacheName = "user-exists")
  boolean userExists(String username);

  void createUser(UserRepresentation userRepresentation, List<String> myndRoles);
}
