package de.thi.mynd.common.security;

import io.quarkus.logging.Log;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Central security service for attribute-based access control. Delegates voting to all CDI-managed
 * {@link Voter} beans, following a first-match strategy — the first voter that {@link
 * Voter#supports(String, Object) supports} the given attribute and object determines the outcome.
 *
 * <p>Inspired by Symfony's Security Voters pattern.
 */
@ApplicationScoped
public final class SecurityService {

  @Inject Instance<Voter<?>> voters;

  @Inject SecurityIdentity identity;

  /**
   * Checks whether the current principal is granted the given attribute on the provided object.
   * Iterates all registered {@link Voter} beans and delegates to the first one that declares
   * support for the attribute/object combination.
   *
   * @param object the object to check access against (e.g. an entity or DTO)
   * @param attribute the permission attribute to evaluate (e.g. {@code "EDIT"}, {@code "DELETE"})
   * @return {@code true} if a matching voter grants access, {@code false} if none match or the
   *     matching voter denies access
   */
  public boolean isGranted(Object object, String attribute) {
    for (Voter voter : voters) {
      if (voter.supports(attribute, object)) {
        Log.debugf("Voting on %s with attribute %s", object.getClass(), attribute);
        return voter.vote(identity, attribute, object);
      }
    }
    return false;
  }

  /**
   * Enforces that the current principal is granted the given attribute on the provided object.
   * Delegates to {@link #isGranted(Object, String)} and throws if access is denied.
   *
   * @param subject the object to check access against
   * @param attribute the permission attribute to evaluate
   * @throws io.quarkus.security.ForbiddenException if access is not granted
   */
  public void denyUnlessGranted(Object subject, String attribute) {
    if (!isGranted(subject, attribute)) {
      throw new ForbiddenException("Forbidden.");
    }
  }
}
