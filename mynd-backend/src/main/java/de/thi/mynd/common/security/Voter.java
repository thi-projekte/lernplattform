package de.thi.mynd.common.security;

import io.quarkus.security.identity.SecurityIdentity;

/**
 * Strategy interface for attribute-based access control decisions. Each implementation is
 * responsible for a specific domain object type and set of permission attributes. Voters are
 * discovered and invoked by {@link SecurityService} via CDI.
 *
 * <p>Implement this interface as an {@code @ApplicationScoped} CDI bean to register a voter. Only
 * the first voter that {@link #supports} the given attribute/subject combination will be consulted.
 *
 * @param <T> the type of subject this voter handles
 */
public interface Voter<T> {

  /**
   * Determines whether this voter is responsible for the given attribute and subject. Called by
   * {@link SecurityService} before {@link #vote} to find the appropriate voter.
   *
   * @param attribute the permission attribute to evaluate (e.g. {@code "EDIT"}, {@code "DELETE"})
   * @param subject the object access is being checked against
   * @return {@code true} if this voter can evaluate the attribute/subject combination
   */
  boolean supports(String attribute, Object subject);

  /**
   * Evaluates whether the given identity should be granted the specified attribute on the provided
   * subject. Only called if {@link #supports} returned {@code true}.
   *
   * @param identity the current principal's security identity
   * @param attribute the permission attribute to evaluate
   * @param subject the object access is being checked against, guaranteed to be of type {@code T}
   * @return {@code true} if access is granted, {@code false} otherwise
   */
  boolean vote(SecurityIdentity identity, String attribute, T subject);
}
