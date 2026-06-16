/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.security;

import io.quarkus.logging.Log;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public final class SecurityService {

  @Inject Instance<Voter<?>> voters;

  @Inject SecurityIdentity identity;

  public boolean isGranted(Object object, String attribute) {
    for (Voter voter : voters) {
      if (voter.supports(attribute, object)) {
        Log.debugf("Voting on %s with attribute %s", object.getClass(), attribute);
        return voter.vote(identity, attribute, object);
      }
    }
    return false;
  }

  public void denyUnlessGranted(Object subject, String attribute) {
    if (!isGranted(subject, attribute)) {
      throw new ForbiddenException("Forbidden.");
    }
  }
}
