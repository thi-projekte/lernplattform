/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.security;

import de.thi.mynd.common.entity.BaseEntity;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;

@ApplicationScoped
public final class EntityOwnerPrePersistListener {

  @Inject SecurityIdentity securityIdentity;

  @PrePersist
  public void setOwner(Object entity) {
    if (entity instanceof BaseEntity baseEntity) {
      if (baseEntity.creatorId == null && !securityIdentity.isAnonymous()) {
        baseEntity.creatorId = securityIdentity.getPrincipal().getName();
      } else {
        // Dev fallback
        if (baseEntity.creatorId == null) {
          baseEntity.creatorId = "admin";
        }
      }
    }
  }
}
