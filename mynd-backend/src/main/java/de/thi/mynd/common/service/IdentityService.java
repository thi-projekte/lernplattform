package de.thi.mynd.common.service;

import io.quarkus.cache.CacheResult;

public interface IdentityService {

    @CacheResult(cacheName = "external-user")
    String getFullNameByUsername(String username);
}
