package de.thi.mynd.common.security;

import io.quarkus.security.identity.SecurityIdentity;

public interface Voter<T> {

  boolean supports(String attribute, Object subject);

  boolean vote(SecurityIdentity identity, String attribute, T subject);
}
