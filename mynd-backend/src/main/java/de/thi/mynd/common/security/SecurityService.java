package de.thi.mynd.common.security;

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
