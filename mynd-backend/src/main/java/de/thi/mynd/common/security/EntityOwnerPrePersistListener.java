package de.thi.mynd.common.security;

import de.thi.mynd.common.entity.BaseEntity;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class EntityOwnerPrePersistListener {

  @Inject SecurityIdentity securityIdentity;

  public void setOwner(Object entity) {
    if (entity instanceof BaseEntity baseEntity) {
      if (baseEntity.creatorId == null && !securityIdentity.isAnonymous()) {
        baseEntity.creatorId = securityIdentity.getPrincipal().getName();
      }
    }
  }
}
